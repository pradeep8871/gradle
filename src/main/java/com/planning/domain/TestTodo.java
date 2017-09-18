package com.planning.domain;

import org.springframework.data.annotation.Id;

public final class TestTodo {
	
    @Id
    private String id;

    private String description;

    private String title;

    public TestTodo() {}

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public void update(String title, String description) {
        this.title = title;
        this.description = description;
    }

    @Override
    public String toString() {
        return String.format(
                "Todo[id=%s, description=%s, title=%s]",
                this.id,
                this.description,
                this.title
        );
    }

}
