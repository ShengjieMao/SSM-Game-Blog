package com.sj.domain.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class MenuTreeVo {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String label;

    private Long parentId;

    private List<MenuTreeVo> children;
}