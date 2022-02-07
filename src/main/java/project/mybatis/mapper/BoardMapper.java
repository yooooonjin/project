package project.mybatis.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;

import project.domain.dto.helpBoard.HelpBoardListDto;
import project.domain.dto.helpBoard.HelpBoardWriteDto;

@Mapper
public interface BoardMapper {

	List<HelpBoardListDto> boardList();

	void boadSave(HelpBoardWriteDto writeDto);

	List<HelpBoardListDto> boardCategoryList(String category);


	List<HelpBoardListDto> boardSubjectKeywordList(String keyword);
	List<HelpBoardListDto> boardEmailKeywordList(String keyword);

}
