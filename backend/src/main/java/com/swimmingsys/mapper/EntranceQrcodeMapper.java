package com.swimmingsys.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swimmingsys.model.entity.EntranceQrcode;
import org.apache.ibatis.annotations.Mapper;

/**
 * 入场二维码Mapper接口
 */
@Mapper
public interface EntranceQrcodeMapper extends BaseMapper<EntranceQrcode> {
}
