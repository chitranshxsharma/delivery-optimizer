package com.delivery.dto;
import lombok.Data;
@Data
public class NodeResponse {
    public Long id;
    public String code;
    public String name;
    public String type;
    public Double latitude;
    public Double longitude;
    public String city;
    public boolean active;
}
