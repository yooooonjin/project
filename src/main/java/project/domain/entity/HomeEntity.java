package project.domain.entity;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.ColumnDefault;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Table(name="home")
@Entity
public class HomeEntity extends BaseEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long hno;
	
	@Column(nullable = false)
	private String homeName;
	@Column(nullable = false)
	private String homeAddress;
	@Column(nullable = false)
	private long homePrice;
	
	@Column(nullable = false)
	private int maximumNumber;
	@Column(nullable = false)
	private int bedNumber;
	@Column(nullable = false)
	private int bedroomNumber;
	@Column(nullable = false)
	private int bathroomNumber;
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private HomeType homeType;
	
	@Column(nullable = false)
	private String homeIntro;
	
	@Column(nullable = false)
	private String useable;
	
	@Enumerated(EnumType.STRING)
	@ElementCollection(fetch = FetchType.EAGER)
	@Builder.Default
	private Set<HomeOption> homeOptionSet= new HashSet<HomeOption>();
	public void addHomeOption(HomeOption homeOption) {
		homeOptionSet.add(homeOption);
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "email")
	private MemberEntity member;
	
	@OneToMany(mappedBy ="home")
	@Builder.Default
	private List<ReservationEntity> reservations = new Vector<>();
	
	@OneToMany(mappedBy ="home")
	@Builder.Default
	private List<HomeImageEntity> homeImages = new Vector<>();
	
	
	//?????? ????????? ????????? ?????? ????????? ??????
	public boolean isreservations(LocalDate checkin, LocalDate checkout) {
		
		reservations=reservations.stream()
		.filter(r->r.isReservation(checkin, checkout)) //????????? ??????????????? //????????? ??????????????????
		.collect(Collectors.toList()); //????????? ????????? ????????? ???????????? List??? ??????
		
		if(reservations.size()>0) {
			return false; //????????? ????????? ????????? ???????????? ?????? ??????X
		}	
		return true;
	}
	

}
