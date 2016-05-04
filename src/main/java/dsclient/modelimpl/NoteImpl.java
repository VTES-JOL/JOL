package dsclient.modelimpl;

import nbclient.model.Note;

class NoteImpl implements Note {

    private final String name;
    private String value;
    NoteImpl(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

}
