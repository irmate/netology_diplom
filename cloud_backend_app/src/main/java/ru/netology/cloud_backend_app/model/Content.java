package ru.netology.cloud_backend_app.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "content")
public class Content extends BaseEntity {
    @Column(name = "name")
    private String name;
    @Column(name = "data")
    @Lob
    private byte[] data;
    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;
}