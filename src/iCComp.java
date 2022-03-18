public class iCComp {
    public String iCCName;
    public String sourceComp;
    public String targetComp;
    public String typeComm;
    public int userICount = 0;
    public int androidAPICount = 0;
    public int javaAPICount = 0;

    /*
    iCComp(String iCCName, String sourceComp, String targetComp, String typeComm){
        this.iCCName = iCCName;
        this.sourceComp = sourceComp;
        this.targetComp = targetComp;
        this.typeComm = typeComm;
    }
    **/

    iCComp(String iCCName){
        this.iCCName = iCCName;
    }

    public String getICCName() {
        return iCCName;
    }
    public void setiCCName(String iCCName) {
        this.iCCName = iCCName;
    }
    
    public void setSourceComp(String sourceComp){
        this.sourceComp = sourceComp;
    }

    public String getSourceComp() {
        return sourceComp;
    }
    
    public void setTargetComp(String targetComp){
        this.targetComp = targetComp;
    }

    public String getTargetComp() {
        return targetComp;
    }
    public void setTypeComm(String typeComm){
        this.typeComm = typeComm;
    }

    public String getTypeComm() {
        return typeComm;
    }

    public void userICount(int userICount){
        this.userICount = userICount;
    }

    public int getuserICount(){
        return userICount;
    }

    public void androidAPICount(int androidAPICount){
        this.androidAPICount = androidAPICount;
    }

    public int getandroidAPICount(){
        return androidAPICount;
    }
    public void javaAPICount(int javaAPICount){
        this.javaAPICount = javaAPICount;
    }

    public int getjavaAPICount(){
        return javaAPICount;
    }

    @Override
    public String toString() {
        return "{" +
            " iCCName='" + getICCName() + "'" +
            ", sourceComp='" + getSourceComp() + "'" +
            ", targetComp='" + getTargetComp() + "'" +
            ", typeComm='" + getTypeComm() + "'" +
            "}";
    }

    public String toCSVFormat() {
        return getICCName() + "," + getSourceComp() + "," + getTargetComp() + "," + getTypeComm() + "," + getandroidAPICount() + "," + getjavaAPICount() + "," + getuserICount() + "\n";
    }
    
    
}
