public class FileRequest extends AbstractMessage {
    private String fileName;

    public String getName(){
        return fileName;
    }
     //переименовать файл
    public FileRequest(String fileName){
        this.fileName = fileName;
    }
}
