package net.griddb;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "greet", mixinStandardHelpOptions = true, version = "greet 1.0",
         description = "Prints a greeting message.")
class GreetCommand implements Runnable {

    @Option(names = {"-n", "--name"}, description = "The name of the person to greet.")
    private String name = "World";

    public void run() {
        System.out.printf("Hello, %s!%n", name);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new GreetCommand()).execute(args);
        System.exit(exitCode);
    }
}

public class Main {
    public static void main(String[] args) {
        System.out.println("Hello world!");
    }
}