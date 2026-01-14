package com.timothymarias.cookingapp.entity

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "quantities")
class Quantity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(nullable = false)
    var amount: Double,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id", nullable = false)
    var unit: Unit,

    // For sync engine: store the frontend's local_id
    @Column(name = "local_id", unique = true)
    var localId: String? = null,

    @Column(nullable = false)
    var version: Int = 1,

    @Column(name = "last_modified", nullable = false)
    var lastModified: Instant = Instant.now()
) {
    constructor() : this(null, 0.0, Unit(), null, 1, Instant.now())
}