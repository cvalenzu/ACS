/*******************************************************************************
 * Copyright (c) 2010 - 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Lars Vogel <lars.Vogel@gmail.com> - Bug 419770
 *******************************************************************************/
package alma.acs.alarmsystemprofiler.handlers;

import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.swt.widgets.Shell;

public class SaveHandler {

//	@CanExecute
//	public boolean canExecute(EPartService partService) {
//		if (partService != null) {
//			return !partService.getDirtyParts().isEmpty();
//		}
//		return false;
//	}

	@Execute
	public void execute(Shell shell) {
		System.out.println("Saving...");
	}
}