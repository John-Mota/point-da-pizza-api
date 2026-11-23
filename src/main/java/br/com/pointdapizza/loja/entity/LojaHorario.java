package br.com.pointdapizza.loja.entity;

import io.quarkus.hibernate.orm.panache.PanacheEntityBase;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "loja_horario")
public class LojaHorario extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "loja_id", nullable = false)
    public Loja loja;

    @Column(name = "dia", nullable = false)
    public Short dia;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "abre", nullable = false, length = 5, columnDefinition = "char(5)")
    public String abre;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "fecha", nullable = false, length = 5, columnDefinition = "char(5)")
    public String fecha;
}
