package deckserver.client;

/**
 * Created by shannon on 23/08/2016.
 */
class CommandException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = -1426332705239199332L;

    CommandException(String msg) {
        super(msg);
    }
}
