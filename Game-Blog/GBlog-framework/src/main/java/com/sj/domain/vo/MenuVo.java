package com.sj.domain.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors(chain = true)
public class MenuVo {
    @TableId(type = IdType.AUTO)
    private Long id;

    private String menuName;

    private Long parentId;

    private Integer orderNum;

    private String path;

    private String component;

    private Integer isFrame;

    private String menuType;

    private String visible;

    private String status;

    private String perms;

    private String icon;

    private String remark;

}