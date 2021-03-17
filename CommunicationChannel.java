import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Class that implements the channel used by headquarters and space explorers to communicate.
 */
public class CommunicationChannel {

    private BlockingQueue<Message> spaceExplorers_Channel;
    private BlockingQueue<Message> headQuarters_Channel;
    private ReentrantLock writeLock = new ReentrantLock();
    private ReentrantLock readLock = new ReentrantLock();
    public Semaphore spEx = new Semaphore(1);
    private Integer locked_times = 0;
    private Integer messages_sent = 0;

    /**
     * Creates a {@code CommunicationChannel} object.
     */
    CommunicationChannel() {
        spaceExplorers_Channel = new LinkedBlockingQueue<>();
        headQuarters_Channel = new LinkedBlockingQueue<>();
    }
    /**
     * Puts a message on the space explorer channel (i.e., where space explorers write to and
     * headquarters read from).
     *
     * @param message
     *            message to be put on the channel
     */
    void putMessageSpaceExplorerChannel(Message message){
        try {
            spaceExplorers_Channel.put(message);
        } catch (Exception ignored) { }
    }

    /**
     * Gets a message from the space explorer channel (i.e., where space explorers write to and
     * headquarters read from).
     *
     * @return message from the space explorer channel
     */
    public Message getMessageSpaceExplorerChannel() {
        try{
            return spaceExplorers_Channel.take();
        }catch (Exception ignored){ }
        return null;
    }

    /**
     * Puts a message on the headquarters channel (i.e., where headquarters write to and
     * space explorers read from).
     *
     * @param message
     *            message to be put on the channel
     */
    public void putMessageHeadQuarterChannel(Message message) {
        String data = message.getData();
        if(data.contains("END"))
            return;
        else if(data.contains("EXIT")){
            try {
                headQuarters_Channel.put(message);
            } catch (Exception ignored) {}
        }
        else {
            writeLock.lock();
            try{
                headQuarters_Channel.put(message);
                locked_times++;
            } catch (Exception ignored){
            }
            if(locked_times == 2) {
               writeLock.unlock();
               writeLock.unlock();
                locked_times = 0;
            }
            }

        }


    /**
     * Gets a message from the headquarters channel (i.e., where headquarters write to and
     * space explorer read from).
     *
     * @return message from the header quarter channel
     */
    public Message getMessageHeadQuarterChannel() {
        Message message = null;
        readLock.lock();
        try{
            message = headQuarters_Channel.take();
            messages_sent ++;
        } catch (Exception ignored){}
        if(messages_sent == 2){
            readLock.unlock();
            readLock.unlock();
            messages_sent = 0;
        }
        return message;
    }
}
