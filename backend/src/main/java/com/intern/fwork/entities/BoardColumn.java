package com.intern.fwork.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "columns")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoardColumn {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String UUID;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String position;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id", nullable = false)
    private Board board;

    @OneToMany(mappedBy = "column",cascade = CascadeType.ALL,orphanRemoval = true)
    @OrderBy("position ASC")
    @Builder.Default
    private List<Task> tasks = new ArrayList<>();
}
