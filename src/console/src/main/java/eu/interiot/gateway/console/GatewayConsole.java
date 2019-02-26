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
package eu.interiot.gateway.console;

import java.io.PrintWriter;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.osgi.framework.BundleContext;

import eu.interiot.gateway.commons.api.command.CommandLine;
import eu.interiot.gateway.commons.api.command.CommandLine.Help;
import eu.interiot.gateway.commons.api.command.CommandService;
import eu.interiot.gateway.commons.api.services.CoreService;
import eu.interiot.gateway.console.api.JLineAppender;
import jline.console.ConsoleReader;

public class GatewayConsole implements CoreService{
	
	private static Logger log = LogManager.getLogger("Console");
	
	private CommandService commandService;
	
	public GatewayConsole(BundleContext context) throws Exception{
		this.commandService = context.getService(context.getServiceReference(CommandService.class));
	}
	
	@Override
	public void run() {
		try {
			
			ConsoleReader reader = new ConsoleReader();
			
			Commands commands = new Commands(reader, this.commandService);
			this.commandService.registerCommand(Commands.Log.class);
			this.commandService.registerCommand(Commands.Help.class, commands.getHelpFactory());
			this.commandService.registerCommand(Commands.Clear.class, commands.getClearFactory());
			
			reconfigureRootLogger(reader);
			
            reader.setPrompt(
	            "\u001B[31mg\u001B[0m"+
	            "\u001B[32ma\u001B[0m"+		
	            "\u001B[33mt\u001B[0m"+
	            "\u001B[34me\u001B[0m"+		
	            "\u001B[35mw\u001B[0m"+
	            "\u001B[36ma\u001B[0m"+		
	            "\u001B[37my\u001B[0m"+	
            	"> "
            );
            //List<Completer> completors = new LinkedList<Completer>();
            //completors.add(new AnsiStringsCompleter("\u001B[1mfoo\u001B[0m", "bar", "\u001B[32mbaz\u001B[0m"));
            //CandidateListCompletionHandler handler = new CandidateListCompletionHandler();
            //handler.setStripAnsi(true);
            //reader.setCompletionHandler(handler);
            
            String line;
            PrintWriter out = new PrintWriter(reader.getOutput());
            
            while ((line = reader.readLine()) != null) {
            	
            	//out.println("\u001B[33m======>\u001B[0m\"" + line + "\"");
                //out.flush();
                
                String [] elements = line.split("[\\s\\xA0]+");
                
                if(elements.length == 0) continue;
                
                String commandName = elements[0];
                String [] args;
                
                try{
                	args = Arrays.copyOfRange(elements, 1, elements.length);
                }catch(Exception ex){
                	args = new String[]{};
                }
                
                if(commandName.trim().equals("")) continue;
                
                CommandLine command = this.commandService.getLinkedCommand(commandName, out);
                
                if(command == null) {
                	log.error("Invalid command - type 'help' to get a list of available commands");
                } else {
                	try {
                		CommandLine.call(command, out, Help.Ansi.AUTO, args);
                	}catch(Exception ex) {
                		log.error(ex);
                		ex.printStackTrace();
                	}
                }
                
                out.flush();
                
            }
            reader.close();
        }
        catch (Throwable t) {
            t.printStackTrace();
        }
	}
	
	private void reconfigureRootLogger(ConsoleReader reader) {
		log.info("Reconfiguring Appender...");
		LoggerConfig rootLogger = ((org.apache.logging.log4j.core.Logger)log).getContext().getConfiguration().getRootLogger();
		Appender consoleAppender = rootLogger.getAppenders().get("Console");
		Appender appender = new JLineAppender("JLineAppender", null, consoleAppender.getLayout(), true, reader);
		//Appender appender = MyCustomAppenderImpl.createAppender("JLineAppender", consoleAppender.getLayout(), null, null);
		appender.start();
		rootLogger.addAppender(appender, rootLogger.getLevel(), rootLogger.getFilter());
		rootLogger.removeAppender("Console");
	}
	
}
