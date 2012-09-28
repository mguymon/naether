package com.tobedevoured.naether.osgi;

public interface Command {
    public String getName();
    public String getDescription();
    public boolean execute(String commandline);
}