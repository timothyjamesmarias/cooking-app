package com.timothymarias.cookingapp.entity

import jakarta.persistence.*

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
    var localId: String? = null
) {
    constructor() : this(null, 0.0, Unit(), null)
}