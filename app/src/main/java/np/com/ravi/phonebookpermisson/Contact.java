package np.com.ravi.phonebookpermisson;

/**
 * Created by ravi on 12/22/17.
 */

public class Contact {
    //POJO
    String name;
    String phoneNumber;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    @Override
    public String toString() {
//        return super.toString();
        return (this.name + "\n" + this.phoneNumber);
    }
}
