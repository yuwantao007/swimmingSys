package com.swimmingsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.swimmingsys.model.dto.CoachAddDTO;
import com.swimmingsys.model.dto.CoachQueryDTO;
import com.swimmingsys.model.dto.CoachUpdateDTO;
import com.swimmingsys.model.vo.CoachVO;

import java.util.List;

/**
 * 教练服务接口
 */
public interface CoachService {

    /**
     * 获取教练列表（分页/条件查询）
     *
     * @param queryDTO 查询条件
     * @return 教练分页列表
     */
    IPage<CoachVO> getCoachList(CoachQueryDTO queryDTO);

    /**
     * 根据ID获取教练信息
     *
     * @param id 教练ID
     * @return 教练信息
     */
    CoachVO getCoachById(Long id);

    /**
     * 新增教练
     *
     * @param addDTO 新增教练信息
     * @return 新增的教练信息
     */
    CoachVO addCoach(CoachAddDTO addDTO);

    /**
     * 更新教练信息
     *
     * @param id        教练ID
     * @param updateDTO 更新信息
     * @return 更新后的教练信息
     */
    CoachVO updateCoach(Long id, CoachUpdateDTO updateDTO);

    /**
     * 删除教练（逻辑删除）
     *
     * @param id 教练ID
     * @return 是否删除成功
     */
    boolean deleteCoach(Long id);

    /**
     * 获取在职教练列表（用于下拉选择）
     *
     * @return 在职教练列表
     */
    List<CoachVO> getActiveCoachList();
}
