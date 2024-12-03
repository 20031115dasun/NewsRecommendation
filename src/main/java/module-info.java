module com.example.oodcw {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires org.apache.opennlp.tools;
    requires org.json;
    requires org.apache.httpcomponents.client5.httpclient5.fluent;
    requires jbcrypt;


    opens com.example.oodcw to javafx.fxml;
    exports com.example.oodcw;
}