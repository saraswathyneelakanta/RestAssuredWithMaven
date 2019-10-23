package invitation;

public class InvitationRequest {
	
	private int invitationId;
	
	private ApplicationData applicationData;
	private UserData userData;
	private InviteOption inviteOption;
	
	public ApplicationData getApplicationData() {
		return applicationData;
	}
	public void setApplicationData(ApplicationData applicationData) {
		this.applicationData = applicationData;
	}
	public UserData getUserData() {
		return userData;
	}
	public void setUserData(UserData userData) {
		this.userData = userData;
	}
	public InviteOption getInviteOption() {
		return inviteOption;
	}
	public void setInviteOption(InviteOption inviteOption) {
		this.inviteOption = inviteOption;
	}
	
	public int getInvitationId() {
		return invitationId;
	}
	public void setInvitationId(int invitationId) {
		this.invitationId = invitationId;
	}
}
