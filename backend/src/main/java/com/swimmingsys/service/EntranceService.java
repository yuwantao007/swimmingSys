package com.swimmingsys.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.swimmingsys.model.dto.EntranceRecordQueryDTO;
import com.swimmingsys.model.dto.EntranceVerifyDTO;
import com.swimmingsys.model.entity.User;
import com.swimmingsys.model.vo.EntranceQrcodeVO;
import com.swimmingsys.model.vo.EntranceRecordVO;
import com.swimmingsys.model.vo.EntranceVerifyResultVO;

/**
 * 入场服务接口
 */
public interface EntranceService {

    /**
     * 生成入场二维码
     *
     * @param loginUser 当前登录用户（会员）
     * @return 二维码信息
     */
    EntranceQrcodeVO generateEntranceQrcode(User loginUser);

    /**
     * 验证入场二维码
     *
     * @param verifyDTO 验证请求
     * @param verifier  验证人（管理员）
     * @return 验证结果
     */
    EntranceVerifyResultVO verifyEntrance(EntranceVerifyDTO verifyDTO, User verifier);

    /**
     * 获取我的入场记录（会员）
     *
     * @param queryDTO  查询条件
     * @param loginUser 当前登录用户
     * @return 入场记录分页列表
     */
    IPage<EntranceRecordVO> getMyEntranceRecords(EntranceRecordQueryDTO queryDTO, User loginUser);

    /**
     * 获取所有入场记录（管理员）
     *
     * @param queryDTO 查询条件
     * @return 入场记录分页列表
     */
    IPage<EntranceRecordVO> getAllEntranceRecords(EntranceRecordQueryDTO queryDTO);

    /**
     * 根据ID获取入场记录详情
     *
     * @param id 记录ID
     * @return 入场记录信息
     */
    EntranceRecordVO getEntranceRecordById(Long id);

    /**
     * 删除入场记录（管理员）
     * 逻辑删除
     *
     * @param id 记录ID
     * @return 是否删除成功
     */
    boolean deleteEntranceRecord(Long id);
}
