package models;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import siena.Generator;
import siena.Id;
import siena.Model;
import siena.Query;
import siena.Table;
import siena.Time;

import com.google.appengine.api.datastore.Blob;

@Table("uploads")
public class Upload extends Model {
    
    @Id(Generator.AUTO_INCREMENT)
    public Long id;
    
    @Time
    public Date created = Calendar.getInstance().getTime();
    
    public Blob file;
    
    public String contenttype;
    
    public String filename;
    
    public String originalname;
    
    public Boolean secure = false;
    
    public static Query<Upload> all() {
        return Model.all(Upload.class);
    }

}
