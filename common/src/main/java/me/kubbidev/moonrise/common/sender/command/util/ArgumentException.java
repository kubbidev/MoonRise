package me.kubbidev.moonrise.common.sender.command.util;

import me.kubbidev.moonrise.common.sender.Sender;
import me.kubbidev.moonrise.common.sender.command.abstraction.Command;
import me.kubbidev.moonrise.common.sender.command.abstraction.CommandException;

public abstract class ArgumentException extends CommandException {

    public static class DetailedUsage extends ArgumentException {

        @Override
        protected void handle(Sender sender) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void handle(Sender sender, String label, Command command) {
            command.sendDetailedUsage(sender, label);
        }
    }
}
