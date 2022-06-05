package com.dream.pojo;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableField;
import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <p>
 *
 * </p>
 *
 * @author zwq
 * @since 2022-05-26
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class MComment implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String nickname;

    private String email;

    private String content;

    private String avatar;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(fill = FieldFill.INSERT)
    private Date created;

    private String ip;

    @TableField("isAdminComment")
    private Integer isAdminComment;

    @TableField("blogId")
    private Integer blogId;

    @TableField("parentCommentNickname")
    private String parentCommentNickname;

    @TableField("parentCommentId")
    private Integer parentCommentId;

    private String website;

    private String qq;

    @TableField(exist = false)
    private List<MComment> replyComments;

}
