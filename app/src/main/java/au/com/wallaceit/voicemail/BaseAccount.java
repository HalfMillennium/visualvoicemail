package au.com.wallaceit.voicemail;

public interface BaseAccount {
    public String getPhoneNumber();
    public void setPhoneNumber(String phoneNumber);
    public String getDescription();
    public void setDescription(String description);
    public String getUuid();
}
