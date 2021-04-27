package com.example.mytodo;

import java.io.Serializable;
import java.util.Date;

public class Task implements Serializable {
    public String name;
    public String description;
    public int hardness;
    public int progress;
    public String deadline;

    public Task(String name, String description, int hardness, int progress, String deadline)
    {
        this.name = name;
        this.description = description;
        this.hardness = hardness;
        this.progress = progress;
        this.deadline = deadline;
    }
}
