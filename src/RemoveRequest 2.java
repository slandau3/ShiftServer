import java.io.Serializable;

/**
 * Created by slandau on 10/22/16.
 */
public class RemoveRequest implements Serializable {
    
    private Contact c;
    
    public RemoveRequest(Contact c) {
        this.c = c;
    }
    
    public Contact getContact() {
        return this.c;
    }
}
