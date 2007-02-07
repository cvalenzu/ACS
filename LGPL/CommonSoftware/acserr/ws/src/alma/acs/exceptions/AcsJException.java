/*
 *    ALMA - Atacama Large Millimiter Array
 *    (c) European Southern Observatory, 2002
 *    Copyright by ESO (in the framework of the ALMA collaboration),
 *    All rights reserved
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation; either
 *    version 2.1 of the License, or (at your option) any later version.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 *
 *    You should have received a copy of the GNU Lesser General Public
 *    License along with this library; if not, write to the Free Software
 *    Foundation, Inc., 59 Temple Place, Suite 330, Boston, 
 *    MA 02111-1307  USA
 */
package alma.acs.exceptions;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

import org.omg.CORBA.UserException;

import alma.ACSErr.ACSException;
import alma.ACSErr.ErrorTrace;
import alma.ACSErr.NameValue;
import alma.ACSErr.Severity;
import alma.acs.util.UTCUtility;

/**
 * Base class for any checked exceptions defined in ALMA Java code.
 * Allows these Java exceptions to be interoperable with the ACS error system,
 * i.e. that they can be converted to and from {@link ErrorTrace} or
 * {@link ACSException} objects. 
 * See {@link #AcsJException(ErrorTrace)}, {@link #getErrorTrace}, {@link #getACSException}. 
 * <p>
 * Not to be confused with <code>alma.ACSErr.ACSException</code>,
 * which is a CORBA user exception with an <code>alma.ACSErr.ErrorTrace</code> inside.
 * <code>ACSException</code> is declared as <code>final</code>,
 * so that it's not an option to make our <code>AcsJException</code>
 * a subclass of it. (todo check if that's due to OMG spec or JacORB).
 * <p>
 * For the conversion to and from the CORBA exception types, 
 * two <code>ErrorTrace</code> properties (i.e. {@link NameValue} objects
 * referenced from {@link ErrorTrace#data}) are added:
 * <ul>
 * <li> <code>javaex.class</code> to store the exception class for later reconstruction
 * <li> <code>javaex.msg</code> to store the message text provided in the
 * 		various constructors of <code>Throwable</code> and subclasses.<br>
 * 		TODO check why there is no such field available already in ErrorTrace,
 * 		is there really no unique way of transporting text messages in addition
 * 		to error types and codes??
 * </ul>
 * <p>
 * 
 * @author hsommer Jun 18, 2003 1:07:38 PM
 */
public abstract class AcsJException extends Exception
{
	private static String s_thisHost;
	// process name or PID, container name if applicable
	private static String s_thisProcess;

	// host and process (possibly converted from remote process!)
	protected String m_host;
	protected String m_process;

	// location data (needed if converted back from ErrorTrace,
	// in which case we don't have VM StackTrace available)
	protected int m_line;
	protected String m_method;
	protected String m_file;
		
	// thread name or its ID
	protected String m_threadName;
	
	protected Severity m_severity;
	
	// additional name-value pairs
	protected Properties m_properties;

	protected long m_timeMilli;

	
	private boolean m_initialized = false;
	


	public AcsJException()
	{
		super();
		init();
	}

	public AcsJException(String message)
	{
		super(message);
		init();
	}

	/**
	 * Constructor from another exception, with a message.
	 * <p>
	 * If <code>cause</code> is a CORBA exception generated by the ACS error system,
	 * then its ErrorTrace is automatically converted to a chain of AcsJ-style exceptions.  
	 *   
	 * @param cause the causing exception
	 * @see CorbaExceptionConverter#convertHiddenErrorTrace(Throwable)
	 */
	public AcsJException(String message, Throwable cause)
	{
		super(message); // cause will be set later once we know of what type it is		
		cause = CorbaExceptionConverter.convertHiddenErrorTrace(cause);
		init();		
		initCause(cause);			
	}

// HSO 2006-08-16: This ctor should no longer be used, as subclass ctors call the version with a message even if it is null,
// to avoid the Throwable#ctor taking the cause.toString() as the message.	
//	/**
//	 * Constructor from another exception.
//	 * <p>
//	 * If <code>cause</code> is a CORBA exception generated by the ACS error system,
//	 * then its ErrorTrace is automatically converted to a chain of AcsJ-style exceptions.  
//	 *   
//	 * @param cause the causing exception
//	 * @see CorbaExceptionConverter#convertHiddenErrorTrace(Throwable)
//	 */
//	public AcsJException(Throwable cause)
//	{
//		super(); // cause will be set later once we know of what type it is		
//		cause = CorbaExceptionConverter.convertHiddenErrorTrace(cause);
//		init();		
//		initCause(cause);			
//	}

	
	public AcsJException(ErrorTrace etCause)
	{
		super();
		init();
		
		Throwable cause = CorbaExceptionConverter.recursiveGetThrowable(etCause);		
		initCause(cause);
	}

	/**
	 * Creates a new exception, with the chain of causing exceptions 
	 * derived from <code>etCause</code>.
	 * 
	 * @param message
	 * @param etCause
	 */
	public AcsJException(String message, ErrorTrace etCause)
	{
		super(message);
		init();
		
		Throwable cause = CorbaExceptionConverter.recursiveGetThrowable(etCause);		
		initCause(cause);
	}
	
	
	/**
	 * Initializes both static (if null) and non-static member variables. 
	 * Only to be called once by all of the ctors.
	 * <p>
	 * todo: for caused-by exceptions that are constructed from their ErrorTrace
	 * representation, this method does not really need to be called; 
	 * perhaps a new package-private ctor should be added which does not
	 * call init()
	 */
	private void init()
	{
		if (m_initialized)
		{
			return;
		}
		
		if (s_thisHost == null)
		{
			try
			{
				s_thisHost = InetAddress.getLocalHost().getHostName();
			}
			catch (UnknownHostException e)
			{
				s_thisHost = "unknown";
			}
		}
		
		if (s_thisProcess == null)
		{
			s_thisProcess = "java " + System.getProperty("java.version");
		}
		
		m_host = s_thisHost;
		m_process = s_thisProcess;
		m_timeMilli = System.currentTimeMillis();
		m_threadName = Thread.currentThread().getName();
		setSeverity();
		
		m_properties = new Properties();
		
		// better to extract this info here (and not when converting to ErrorTrace)
		// so that m_line etc. can be used for the conversion, which will
		// be correct when converting AcsJExceptions 
		// that were previously constructed from an ErrorTrace... 
		StackTraceElement[] stTrElems = getStackTrace();
		if (stTrElems != null && stTrElems[0] != null)
		{
			StackTraceElement stTrEl = stTrElems[0];
			m_line = stTrEl.getLineNumber();
			m_method = stTrEl.getMethodName();
			m_file = stTrEl.getFileName(); // TODO: or use getClassName() to have the Java class with full package info instead of the simple file?
		}
		else
		{
			m_file = m_method = "unknown";
			m_line = -1;
		}

		m_initialized = true;
	}
	
	
	/**
	 * If we have some better name than "java", for example the unique name of 
	 * the java container.
	 * @param name
	 */
	public static void setProcessName(String name)
	{
		s_thisProcess = name;
	}

	/**
	 * Forces a subclass to choose an ACS error type.
	 * @return the error type
	 */
	protected abstract int getErrorType();
	
	/**
	 * Forces a subclass to choose an ACS error code.
	 * @return the error code
	 */
	protected abstract int getErrorCode();


	/**
	 * Returns the short description which is a hardcoded text particular for a (type, code) combination.
	 * The subclass that represents the exception code will have to override this method.
	 * This method is not <code>abstract</code> because the exception class that corresponds to an error type 
	 * (but not an error code) would otherwise have to overload it with returning an empty string.
	 * @returns an empty string, unless overloaded for an error code.
	 */
	public String getShortDescription() {
		return "";
	}


	/**
	 * Creates a CORBA <code>UserException</code> from this exception.
	 * <p>
	 * Subclasses must return their associated <code>UserException</code>
	 * with an <code>ErrorTrace</code> member.
	 * <p>
	 * By convention, subclasses must also implement another method that returns
	 * the correct subtype of <code>UserException</code>. 
	 * No problem with a code generator of course...
	 */
	public abstract UserException toCorbaException();

	/**
	 * Sets the default severity level to {@link Severity#Error}.
	 * May be overridden by a subclass to set the default severity for that exception, 
	 * or may be called from application code to set it for a specific exception instance.
	 */
	public void setSeverity()
	{// todo add parameter
		m_severity = Severity.Error;
	}

	/**
	 * Returns the Severity level currently associated with this exception instance.
	 * @return Severity
	 */
	public Severity getSeverity()
	{
		return m_severity;
	}
	
	/**
	 * Allows extra information to be attached to the exception.
	 * @return     the previous value of the specified key in this property
	 *             list, or <code>null</code> if it did not have one.
	 */
	public Object setProperty(String key, String value) 
	{
		return m_properties.setProperty(key, value);
	}

	/**
	 * @see #setProperty
	 */
	public String getProperty(String key) 
	{
		return m_properties.getProperty(key);
	}

	/**
	 * To be used by {@link #getErrorTrace()} or whoever else 
	 * cares about a <code>NameValue[]</code>.
	 */
	public NameValue[] getNameValueArray()
	{
		NameValue[] nameValArray = new NameValue[m_properties.size()];
		int count = 0;
		for (Iterator iter = m_properties.keySet().iterator(); iter.hasNext();)
		{
			String key = (String) iter.next();
			String value = m_properties.getProperty(key);
			NameValue nameVal = new NameValue(key, value); 
			nameValArray[count] = nameVal;
			count++;
		}
		return nameValArray;
	}

	/**
	 * Gets the timestamp when this exception was created.
	 * 
	 * @return timestamp in milliseconds (Java-UTC time, 1970)
	 */
	public long getTimestampMillis()
	{
		return m_timeMilli;
	}


	/**
	 * Converts this exception (and all "caused-by" throwables it might have)
	 * to an <code>ErrorTrace</code> that links to 
	 * its caused-by <code>ErrorTrace</code> objects.
	 * <p>
	 * To be used whenever the Corba wall is hit and the Java exception must
	 * be converted to an ACS-Corba-exception.
	 */
	public ErrorTrace getErrorTrace()
	{
		if (!m_initialized) {
			throw new IllegalStateException("This exception has not been initialized. " + 
					"Probably a (generated) exception subclass did not call the superclass constructors!");
		}
		
		ErrorTrace et = recursiveGetErrorTrace(this, m_timeMilli);
		return et; 
	}

	
	/**
	 * Translates a <code>Throwable</code> to an <code>ErrorTrace</code>.
	 * <p>
	 * If <code>thr</code> is a subclass of <code>AcsJException</code>,
	 * then the various data fields of the <code>ErrorTrace</code> object
	 * are properly filled. Otherwise defaults are used.
	 * <p>
	 * If <code>thr</code> has other <code>Throwable</code>s chained up
	 * (see {@link Throwable#getCause}) then these will also be translated,
	 * and the resulting <code>ErrorTrace</code> objects will be linked together.
	 * 
	 * @param thr
	 * @return ErrorTrace
	 */
	private static ErrorTrace recursiveGetErrorTrace(Throwable thr, long defaultTimeMilli)
	{
        ErrorTrace et = createSingleErrorTrace(thr, defaultTimeMilli);

		// if present, chain up the underlying exception(s)
		Throwable cause = thr.getCause();
		if (cause != null) {
			ErrorTrace causeErrorTrace = recursiveGetErrorTrace(cause, --defaultTimeMilli);
			if (et.previousError == null || et.previousError.length != 1) {
				et.previousError = new ErrorTrace[1];
			}
			et.previousError[0] = causeErrorTrace;
		}
		else {
			et.previousError = new ErrorTrace[0];
		}
		
		return et;
	}


    /**
     * Creates an <code>ErrorTrace</code> object that represents this exception.
     * Linked exceptions are not considered, so the returned <code>ErrorTrace.previousError</code> is left as <code>null</code>.
     */
    private ErrorTrace createSingleErrorTrace() {
        ErrorTrace et = new ErrorTrace();
        et.host = m_host;
        et.process = m_process;
//        et.sourceObject =  TODO
        et.lineNum = m_line;
        et.routine = m_method;
        et.file = m_file;
        et.errorType = getErrorType();
        et.errorCode = getErrorCode();
        et.severity = getSeverity(); 
        et.thread = m_threadName;
        et.timeStamp = UTCUtility.utcJavaToOmg(m_timeMilli);
        et.data = getNameValueArray();
        et.shortDescription = getShortDescription();
        addExceptionProperties(this, et);
        return et;
    }
    


    /**
     * Converts a Throwable that may not be an AcsJException to an <code>ErrorTrace</code>.
     * <p>
     * For example, <code>thr</code> could be a <code>NullPointerException</code> thrown in the same process and thus not yet converted 
     * to <code>DefaultAcsJException</code>, but wrapped by some subclass of AcsJException which contains the NPE as its cause.
     * 
     * @param defaultTimeMilli  Java-time (1970-based) used to estimate a time stamp if none is available, 
     *                          in order to avoid something from October 1582 in the new ErrorTrace.
     *                          Typically the timestamp from the wrapping AcsJException.
     * @see #createSingleErrorTrace()
     */
    public static ErrorTrace createSingleErrorTrace(Throwable thr, long defaultTimeMilli) {
           
        ErrorTrace et = null;

        if (thr instanceof AcsJException) {
            et = ((AcsJException)thr).createSingleErrorTrace();
            return et;
        }

        et = new ErrorTrace();
        StackTraceElement[] stTrElems = thr.getStackTrace();
        if (stTrElems != null && stTrElems[0] != null) {
                StackTraceElement stTrEl = stTrElems[0];
                et.file = stTrEl.getFileName();
                et.routine = stTrEl.getMethodName();
                et.lineNum = stTrEl.getLineNumber();
        }
        else {
                // getStackTrace() only make its best-effort,
                // we might end up without the data...
                et.file = et.routine = "unknown";
                et.lineNum = -1;
        }
        
        // the non-ACS exception we are converting does not contain data for the following fields of ErrorTrace,
        // so we have to use defaults or smart guessing
        et.host = s_thisHost;
        et.process = s_thisProcess;
//        et.sourceObject = TODO
        et.thread = "NA";
        et.timeStamp = UTCUtility.utcJavaToOmg(defaultTimeMilli);
        et.errorType = 9; // = ACSErrTypeJavaNative.value; this code is hardcoded otherwise we'll have problem with bulding and duplication of code 
        et.errorCode = -1;
        et.shortDescription = thr.getClass().getName();
        et.severity = Severity.Error;
        et.data = new NameValue[0];

        addExceptionProperties(thr, et);

        return et;
    }

    private static void addExceptionProperties(Throwable thr, ErrorTrace et) {
        // add Java exception information as properties
        String classname = thr.getClass().getName();
        ErrorTraceManipulator.setProperty(et, CorbaExceptionConverter.PROPERTY_JAVAEXCEPTION_CLASS, classname);
        if (thr.getMessage() != null) {
            ErrorTraceManipulator.setProperty(et, CorbaExceptionConverter.PROPERTY_JAVAEXCEPTION_MESSAGE, thr.getMessage());
        }
    }

    
    
    /**
	 * Creates an <code>ACSException</code>.
	 * Typically to be called from a top-level catch block that must
	 * convert any of the Java exceptions used internally by the Java program
	 * to an IDL type exception that can be thrown on over CORBA.
	 *   
	 * @return ACSException
	 * 
	 * @deprecated  <code>ACSException</code> should no longer be used since 
	 * 				the new error framework generates type-safe corba exceptions
	 * 				with an <code>ErrorTrace</code> inside.
	 */
	public ACSException getACSException()
	{
		ErrorTrace et = getErrorTrace();
		ACSException acsEx = new ACSException(et);
		return acsEx;
	}


	/**
	 * Logs this exception and all causing exceptions.
     * For each such exception, a separate log entry is created, in accordance with the ACS error logging specification.
	 * 
	 * @param logger  the JDK logger to be used for logging this exception. 
	 */
	public void log(Logger logger)
	{
        Throwable thr = this;
        
        // the common ID by which all log messages for the various ErrorTrace objects can later be assembled again
        String stackID = logger.getName() + System.currentTimeMillis();    
        // stackLevel is a running integer counting down from the latest Throwable to the initial Throwable (index=0)
        for (int stackLevel = getTraceDepth()-1; stackLevel >= 0; stackLevel--) {     
            ErrorTrace et = createSingleErrorTrace(thr, m_timeMilli - 1);
            LogRecord logRec = createSingleErrorTraceLogRecord(et, stackID, stackLevel);
            logger.log(logRec);
            if (stackLevel > 0) {
                thr = thr.getCause();
            }  
        }
	}

    /**
     * Creates a {@link LogRecord} with the data from the given {@link ErrorTrace} object.
     * Some data such as thread name, line of code, user-defined properties etc which do not correspond to any 
     * field of <code>LogRecord</code> are stored inside a <code>Map</code> that is set as a parameter of the <code>LogRecord</code>.
     * Later when the ACS logging system will process this <code>LogRecord</code>, it will recognize the special data
     * and turn them into the XML attributes of elements like &lt;Debug&gt;
     * @param et the ErrorTrace input object
     * @param stackID  the stackID that must be the same for all linked <code>ErrorTrace</code> objects.
     * @param stackLevel  0-based index for LogRecords from an ErrorTrace chain.
     * @return the LogRecord that contains the data from the input parameters.
     */
    LogRecord createSingleErrorTraceLogRecord(ErrorTrace et, String stackID, int stackLevel) {
        Level logLevel = ErrorTraceLogLevels.mapErrorLevelToLogLevel(et.severity);
        String message = et.shortDescription.trim();
        message += " (type=" + et.errorType + ", code=" + et.errorCode + ")";
        String usermessage = ErrorTraceManipulator.getProperty(et, CorbaExceptionConverter.PROPERTY_JAVAEXCEPTION_MESSAGE);
        if (usermessage != null && usermessage.trim().length() > 0) {
            message += " :: " + usermessage.trim();
        }            
        // need to explicitly construct a log record with the historical data (normal Logger methods would not allow setting these fields) 
        LogRecord logRec = new LogRecord(logLevel, message);
        logRec.setMillis(UTCUtility.utcOmgToJava(et.timeStamp));
        logRec.setSourceClassName(et.file);
        logRec.setSourceMethodName(et.routine);
        logRec.setLoggerName(et.sourceObject); // the SourceObject will be derived from the logger name in AcsXMLLOgFormatter

        // other fields are encoded in a map and will be extracted by the AcsXMLLOgFormatter before sending the log record over the wire
        // Due to build order problems we can't call LogParameterUtil#createPropertiesMap(), but instead have to repeat that code here;
        // the same is true for the property names which would be available as String constants of LogParameterUtil.
        Map<String, Object> specialLogProperties = new HashMap<String, Object>();
        specialLogProperties.put("isAcsPropertiesMap", Boolean.TRUE);
        specialLogProperties.put("Line", new Long(et.lineNum));
        specialLogProperties.put("ThreadName", et.thread);
        specialLogProperties.put("HostName", et.host);
//        logProperties.put("Context", "???");
        specialLogProperties.put("StackId", stackID);
        specialLogProperties.put("StackLevel", new Long(stackLevel));

        // non-standard properties from ErrorTrace
        Map etProperties = ErrorTraceManipulator.getProperties(et);
        etProperties.remove(CorbaExceptionConverter.PROPERTY_JAVAEXCEPTION_CLASS);
        etProperties.remove(CorbaExceptionConverter.PROPERTY_JAVAEXCEPTION_MESSAGE);

        // set both maps as log parameters. By design there can't be any other parameters which would be lost here
        logRec.setParameters(new Object[] {specialLogProperties, etProperties} );

        return logRec;
    }
    
    /**
     * Gets the total number of linked ("caused-by") exceptions including this one.
     * <p>
     * Not to be confused with the length of {@link Throwable#getStackTrace()}, which
     * refers to the call stack for the current exception, and not to linked causing exception. 
     */
    public int getTraceDepth() {
        int depth = 1;
        Throwable cause = getCause();
        while (cause != null) {
            depth++;
            cause = cause.getCause();
        }
        return depth;
    }
    
	/**
	 * @return Returns the m_file.
	 */
	public String getFile()
	{
		return m_file;
	}
	/**
	 * @return Returns the m_host.
	 */
	public String getHost()
	{
		return m_host;
	}
	/**
	 * @return Returns the m_line.
	 */
	public int getLine()
	{
		return m_line;
	}
	/**
	 * @return Returns the m_method.
	 */
	public String getMethod()
	{
		return m_method;
	}
	/**
	 * @return Returns the m_process.
	 */
	public String getProcess()
	{
		return m_process;
	}
	/**
	 * @return Returns the m_threadName.
	 */
	public String getThreadName()
	{
		return m_threadName;
	}
	
}
