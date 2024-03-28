package caudbs2024;

public class Attribute {
    public String relation_name;
    public String attribute_name;
    public int length;
    Attribute(String r_name, String a_name, int len){
        this.relation_name = r_name;
        this.attribute_name = a_name;
        this.length = len;
    }
}
