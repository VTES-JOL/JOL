package deckserver.rich;

public class ChatView {


    private int chatIndex = 0;
    
    /*
    public void addAdditionalText() {
        Home bean = (Home) getValueBinding("#{home}");
        CoreInputText chat = bean.getChat();
       // chat.
    } */

    public boolean isUptoDate() {
        return true;
//        return getChatBean().getIndex() == chatIndex;
    }

    public String getMessage() {
        return "";
    }

    public void setMessage(String msg) {
        //       getChatBean().addMessage(msg);
        resetMessage();
        doUpdate();
    }

    // hacked up hack to get around the non-incrementality of the components.
    public String getMessages() {
        ChatModel chat = null;//getChatBean();
        chatIndex = chat.getIndex();
        String[] msgs = chat.getMessagesForIndexes(0, chatIndex);
        StringBuffer ret = new StringBuffer();
        for (int i = 0; i < msgs.length; i++) {
            ret.append(msgs[i]);
            ret.append("\n");
        }
        return ret.toString();
    }

    private void resetMessage() {
     /*   Home bean = (Home) getValueBinding("#{backing_home}");  
        bean.getChatInput().resetValue();*/
    }

    private void doUpdate() {
//        Home bean = (Home) getValueBinding("#{backing_home}");  
//        CoreInputHidden trigger = bean.getChatTrigger();
//        trigger.queueEvent(new ValueChangeEvent(trigger,null,""));        
    }

    public int getPollInterval() {
        return 1000;//RefreshInterval.calc(getChatBean().getTimestamp());
    }
//
//    private static int counter = 0;
//    public void processPoll(PollEvent pollEvent) {
//        if(!isUptoDate() || ++counter == 10) {
//            doUpdate();
//        }
//    }
}
