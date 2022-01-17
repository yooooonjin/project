package project.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import project.domain.dto.MemberUpdateDto;
import project.domain.dto.home.HomeListDto;
import project.domain.dto.home.HomeReserveDto;
import project.domain.dto.home.HomeReviewDto;
import project.domain.entity.HomeEntity;
import project.domain.entity.HomeEntityRepository;
import project.domain.entity.HomeReviewEntity;
import project.domain.entity.HomeReviewEntityRepository;
import project.domain.entity.MemberEntity;
import project.domain.entity.MemberEntityRepository;
import project.domain.entity.ReservationEntity;
import project.domain.entity.ReservationEntityRepository;
import project.security.dto.SecurityDto;
import project.service.member.MemberService;

@Log4j2
@RequiredArgsConstructor
@Service
public class MemberServiceImpl implements MemberService {

	private final MemberEntityRepository memberRepository;
	private final HomeEntityRepository homeRepository;
	private final ReservationEntityRepository reservationRepository;
	private final HomeReviewEntityRepository homeReviewRepository;
	
	//프로필 정보
	@Override
	public String memberInfo(Model model, SecurityDto securityDto) {
		
		//회원정보 데이터 //TODO DTO에 담아서 이동
		MemberEntity entity= memberRepository.findById(securityDto.getUsername()).get();
		model.addAttribute("member",entity);
		
		//내가 등록한 집 데이터
		List<HomeEntity> homeEntity = homeRepository.findAllByMember_email(entity.getEmail());
		//dto로 변환
		List<HomeListDto> hostHome= homeEntity.stream().map(HomeListDto::new).collect(Collectors.toList());
		model.addAttribute("hostHome",hostHome);
		
		//예약한 집 데이터 //TODO DTO에 담아서 이동
		List<ReservationEntity> reservationEntity = reservationRepository.findAllByMember_email(entity.getEmail());
		//reservationEntity.stream().map(null).collect(Collectors.toList());
		
		model.addAttribute("reservedHome",reservationEntity);
		
		//내가 쓴 후기 데이터
		List<HomeReviewEntity> homeReviewEntity = homeReviewRepository.findAllByMember_email(entity.getEmail());
		//dto로 변환
		List<HomeReviewDto> homeReview= homeReviewEntity.stream().map(HomeReviewDto::new).collect(Collectors.toList());
		model.addAttribute("homeReview",homeReview);
		
		return "member/member-info";
	}
	//회원정보 수정
	@Transactional
	@Override
	public void memberUpdate(MemberUpdateDto updateDto ,Model model) {
		
		memberRepository.findById(updateDto.getEmail()).map(entity->entity.updateNameAndPhonenumberAndAddress(updateDto)).get();
				
	}
	//숙소예약
	//Dto에서 Entity로 변환할지 결정
	@Override
	public String homeReserve(long hno, SecurityDto securityDto, HomeReserveDto reserveDto) {
		ReservationEntity entity = ReservationEntity.builder()
						.checkIn(reserveDto.getCheckIn()).checkOut(reserveDto.getCheckOut()).guests(reserveDto.getGuests())
						.totalPrice(reserveDto.getTotalPrice()).reserveStatus("ask")
						.member(memberRepository.findById(securityDto.getUsername()).get())
						.home(homeRepository.findById(hno).get())
						.build();
		
		reservationRepository.save(entity);
		
		return "redirect:/member/info";
	}
	
	//후기 작성
	@Override
	public void reviewWrite(long hno,SecurityDto securityDto, String review) {
		HomeReviewEntity homeReviewEntity = HomeReviewEntity.builder().review(review)
						.member(memberRepository.findById(securityDto.getUsername()).get())
						.home(homeRepository.findById(hno).get())		
						.build();
		
		homeReviewRepository.save(homeReviewEntity);
	}

}
