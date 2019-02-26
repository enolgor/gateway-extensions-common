/*
 * Copyright 2016-2018 Universitat Politècnica de València
 * Copyright 2016-2018 Università della Calabria
 * Copyright 2016-2018 Prodevelop, SL
 * Copyright 2016-2018 Technische Universiteit Eindhoven
 * Copyright 2016-2018 Fundación de la Comunidad Valenciana para la
 * Investigación, Promoción y Estudios Comerciales de Valenciaport
 * Copyright 2016-2018 Rinicom Ltd
 * Copyright 2016-2018 Association pour le développement de la formation
 * professionnelle dans le transport
 * Copyright 2016-2018 Noatum Ports Valenciana, S.A.U.
 * Copyright 2016-2018 XLAB razvoj programske opreme in svetovanje d.o.o.
 * Copyright 2016-2018 Systems Research Institute Polish Academy of Sciences
 * Copyright 2016-2018 Azienda Sanitaria Locale TO5
 * Copyright 2016-2018 Alessandro Bassi Consulting SARL
 * Copyright 2016-2018 Neways Technologies B.V.
 *
 * See the NOTICE file distributed with this work for additional information
 * regarding copyright ownership.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.interiot.gateway.console.api;

import java.io.IOException;
import java.io.Serializable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.AppenderLoggingException;

import jline.console.ConsoleReader;
import jline.console.CursorBuffer;

public class JLineAppender extends AbstractAppender {
	private final ReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private ConsoleReader reader;
    private CursorBuffer stashed;
    
    public JLineAppender(String name, Filter filter, Layout<? extends Serializable> layout, final boolean ignoreExceptions, ConsoleReader reader) {
    	super(name, filter, layout, ignoreExceptions);
    	this.reader = reader;
    }

	@Override
	public void append(LogEvent event) {
		readLock.lock();
        try {
        	this.stashLine();
            final byte[] bytes = getLayout().toByteArray(event);
        	this.reader.println(/*"___________\n" + */new String(bytes, "UTF-8")/* + "___________"*/);
        	this.reader.flush();
        	this.unstashLine();
        } catch (Exception ex) {
            if (!ignoreExceptions()) {
                throw new AppenderLoggingException(ex);
            }
        } finally {
            readLock.unlock();
        }
	}
	private void stashLine() {
	    this.stashed = this.reader.getCursorBuffer().copy();
	    try {
	        this.reader.getOutput().write("\u001b[1G\u001b[K");
	        this.reader.flush();
	    } catch (IOException e) {
	        // ignore
	    }
	}

	private void unstashLine() {
	    try {
	        this.reader.resetPromptLine(this.reader.getPrompt(),
	          this.stashed.toString(), this.stashed.cursor);
	    } catch (IOException e) {
	        // ignore
	    }
	}
	
}
