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
import java.util.List;
import java.util.StringJoiner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.IntStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import eu.interiot.gateway.commons.api.command.CommandFactory;
import eu.interiot.gateway.commons.api.command.CommandLine;
import eu.interiot.gateway.commons.api.command.CommandLine.Command;
import eu.interiot.gateway.commons.api.command.CommandLine.Option;
import eu.interiot.gateway.commons.api.command.CommandLine.Parameters;
import eu.interiot.gateway.commons.api.command.CommandService;
import eu.interiot.gateway.commons.api.command.ExecutableCommand;
import jline.console.ConsoleReader;

public class Commands {
	
	private final CommandFactory<Clear> clearFactory;
	
	private final CommandFactory<Help> helpFactory;
	
	public Commands(ConsoleReader reader, CommandService commandService) {
		this.clearFactory = new CommandFactory<Clear>() {

			@Override
			public Clear createInstance() throws Exception {
				return new Clear(reader);
			}
			
		};
		
		this.helpFactory = new CommandFactory<Help>() {

			@Override
			public Help createInstance() throws Exception {
				return new Help(commandService);
			}
			
		};
	}
	
	public CommandFactory<Clear> getClearFactory(){
		return this.clearFactory;
	}
	
	public CommandFactory<Help> getHelpFactory(){
		return this.helpFactory;
	}
	
	@CommandLine.Command(name = "clear", description = "Clears the screen console")
	public static class Clear extends ExecutableCommand{
		
		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;

		private final ConsoleReader reader;
		
		public Clear(ConsoleReader reader) {
			this.reader = reader;
		}
		
		@Override
		public void execute(PrintWriter out) throws Exception {
			this.reader.clearScreen();
		}
		
	}
	
	@CommandLine.Command(name = "help", description = "Displays the available gateway commands")
	public static class Help extends ExecutableCommand{
		
		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;

		private final CommandService commandService;
		
		public Help(CommandService commandService) {
			this.commandService = commandService;
		}
		
		@Override
		public void execute(PrintWriter out) throws Exception {
			out.println("\nList of available commands:\n");
			commandService.listCommands().forEach((name, clazz) -> {
				StringJoiner sj = new StringJoiner("\n");
				sj.add(">" + name);
				Command cmd = clazz.getAnnotation(Command.class);
				if(cmd != null && cmd.description() != null) Arrays.stream(cmd.description()).map(s -> "  " + s).forEach(sj::add);
				out.println(sj.toString());
			});
		}
		
	}
	
	@CommandLine.Command(name = "log", description = "Logs a message in the console")
	public static class Log extends ExecutableCommand{
		
		private static Logger log = LogManager.getLogger("Log");
		
		private static Timer timer = new Timer();

		@Parameters(arity="1...*", description = "The messages to be sent")
		private List<String> messages;
		
		@Option(names = {"-h", "--help"}, usageHelp = true, description = "Show this help message and exit.")
	    private boolean helpRequested;
		
		@Override
		public void execute(PrintWriter out) throws Exception {
			StringJoiner sj = new StringJoiner(" ");
			messages.forEach(sj::add);
			IntStream.rangeClosed(1,5).forEach(i -> timer.schedule(new TimerTask() {
				public void run() {
					log.info(sj.toString());
				}
			}, i*1000L));
		}
		
	}
	
}
