package com.cxin.chronicle.infrastructure.convert;


import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cxin.chronicle.infrastructure.model.dto.UserUpdateRequest;
import com.cxin.chronicle.infrastructure.model.entity.User;
import com.cxin.chronicle.infrastructure.model.vo.LoginUserVO;
import com.cxin.chronicle.infrastructure.model.vo.UserVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class UserConvert {
    public abstract LoginUserVO userToLoginUserVO(User user);

    public abstract UserVO userToUserVO(User user);

    public abstract Page<UserVO> userPageToUserVoPage(Page<User> userPage);

    public abstract User userUpdateRequestToUser(UserUpdateRequest userUpdateRequest);

}
