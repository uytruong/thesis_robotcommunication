package sample;

import javafx.scene.control.ComboBox;

public class TableBean {
    String id;
    ComboBox<String> motion;

    public String getId() {
        return id;
    }

    public TableBean(String id, ComboBox<String> motion) {
        this.id = id;
        this.motion = motion;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ComboBox<String> getMotion() {
        return motion;
    }

    public void setMotion(ComboBox<String> motion) {
        this.motion = motion;
    }
}
