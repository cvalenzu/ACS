#include "bulkDataNTProcessQueue.h"
#include "bulkDataNTDDSLoggable.h"

#include "ACS_BD_Errors.h"
#include <ACS_DDS_Errors.h>
#include <ACSErrTypeCommon.h>

using namespace AcsBulkdata;
using namespace ACS_BD_Errors;
using namespace ACS_DDS_Errors;

Queue::Queue(): itemsWait(itemsWaitMutex){
}

void Queue::push(Item& item) {
	ACE_GUARD(ACE_Thread_Mutex, itemsGuard, itemsMutex);
	items.push(item);
	itemsWait.signal();
}

Item Queue::pop() {
	if (items.empty())
		itemsWait.wait();
	ACE_GUARD(ACE_Thread_Mutex, itemsGuard, itemsMutex);
	Item qi = items.front();
	items.pop();
	return qi;
}

bool Queue::empty() {
	ACE_GUARD(ACE_Thread_Mutex, itemsGuard, itemsMutex);
	return items.size() == 0;
}

void Queue::release() {
	itemsWait.signal();
}

void Queue::clear() {
	ACE_GUARD(ACE_Thread_Mutex, itemsGuard, itemsMutex);
	std::queue<Item> tmp;
	std::swap(items, tmp);
}

ProcessQueue::ProcessQueue(const ACE_CString name): ACS::Thread(name, ThreadBase::defaultResponseTime, ThreadBase::defaultSleepTime, false, THR_NEW_LWP | THR_JOINABLE), callback_mp(NULL), processing(true) {
}

ProcessQueue::~ProcessQueue() {
	//while(!queue.empty()) {
	//	queue.pop();
	//}
	//queue.release();
}

void ProcessQueue::setCallback(BulkDataNTCallback* callback) {
	callback_mp = callback;
}

void ProcessQueue::setTopicName(std::string topicName) {
	topicName_m = topicName;
}

void ProcessQueue::cbStart(unsigned char* userParam_p, unsigned int size) {
	Item qi;
	qi.state = BDState::START;
	if (size > 0)
		memcpy(qi.data, userParam_p, size);//*sizeof(unsigned char));
	qi.size = size;
	queue.push(qi);
}

void ProcessQueue::cbReceive(unsigned char* frame_p, unsigned int size) {
	Item qi;
	qi.state = BDState::RECEIVE;
	if (size > 0)
		memcpy(qi.data, frame_p, size);//*sizeof(unsigned char));
	qi.size = size;
	queue.push(qi);
}

void ProcessQueue::cbStop() {
	Item qi;
	qi.state = BDState::STOP;
	queue.push(qi);
}

void ProcessQueue::cbReset() {
	Item qi;
	qi.state = BDState::RESET;
	queue.push(qi);
}

void ProcessQueue::onError(ACSErr::CompletionImpl &error) {
	Item qi;
	qi.state = BDState::ERROR;
	qi.error = error;
	queue.push(qi);
}

void ProcessQueue::onDataLost(unsigned long frameCount, unsigned long totalFrames, ACSErr::CompletionImpl &error) {
	Item qi;
	qi.state = BDState::DATA_LOST;
	qi.frameCount = frameCount;
	qi.totalFrames = totalFrames;
	qi.error = error;
	queue.push(qi);
}

void ProcessQueue::onSenderConnect(unsigned short totalSenders) {
	Item qi;
	qi.state = BDState::SENDER_CONNECT;
	qi.senders = totalSenders;
	queue.push(qi);
}

void ProcessQueue::onSenderDisconnect(unsigned short totalSenders) {
	Item qi;
	qi.state = BDState::SENDER_DISCONNECT;
	qi.senders = totalSenders;
	queue.push(qi);
}

void ProcessQueue::run() {
	while (processing) {
		Item qi = queue.pop(); //Waits until new element is available
		switch (qi.state) {
			case BDState::START:
				BDNT_LISTENER_USER_ERR(callback_mp->cbStart(qi.data, qi.size));
				break;
			case BDState::RECEIVE:
				BDNT_LISTENER_USER_ERR(callback_mp->cbReceive(qi.data, qi.size));
				break;
			case BDState::STOP:
				BDNT_LISTENER_USER_ERR(callback_mp->cbStop());
				break;
			case BDState::RESET:
				BDNT_LISTENER_USER_ERR(callback_mp->cbReset());
				break;
			case BDState::ERROR:
				callback_mp->onError(qi.error);
				break;
			case BDState::DATA_LOST:
				BDNT_LISTENER_USER_ERR(callback_mp->onDataLost(qi.frameCount, qi.totalFrames, qi.error));
				break;
			case BDState::SENDER_CONNECT:
				BDNT_LISTENER_USER_ERR(callback_mp->onSenderConnect(qi.senders));
				break;
			case BDState::SENDER_DISCONNECT:
				BDNT_LISTENER_USER_ERR(callback_mp->onSenderDisconnect(qi.senders));
				break;
			case BDState::FINISH:
				processing = false;
				break;
			default:
				break;
		}
	}
}

void ProcessQueue::cleanUp() {
	Item qi;
	qi.state = BDState::FINISH;
	queue.push(qi);
}

void ProcessQueue::forceCleanUp() {
	queue.clear();
	this->cleanUp();
}
