declare namespace API {
  type BaseResponseBoolean = {
    code?: number
    data?: boolean
    message?: string
  }

  type BaseResponseListEntriesVo = {
    code?: number
    data?: EntriesVo[]
    message?: string
  }

  type BaseResponseListHeatmapDataVo = {
    code?: number
    data?: HeatmapDataVo[]
    message?: string
  }

  type BaseResponseLoginUserVO = {
    code?: number
    data?: LoginUserVO
    message?: string
  }

  type BaseResponsePageUserVO = {
    code?: number
    data?: PageUserVO
    message?: string
  }

  type BaseResponseString = {
    code?: number
    data?: string
    message?: string
  }

  type BaseResponseUser = {
    code?: number
    data?: User
    message?: string
  }

  type BaseResponseUserVO = {
    code?: number
    data?: UserVO
    message?: string
  }

  type DeleteRequest = {
    id: string
  }

  type EntriesAddReq = {
    content: string
    entryType?: string
  }

  type EntriesDeleteReq = {
    /** 记录ID */
    id: string
  }

  type EntriesHistoryReq = {
    /** 每页大小，默认为 10 */
    pageSize?: number
    /** 上一页最后一条记录的创建时间（首次请求不传） */
    lastCreateTime?: string
  }

  type EntriesQueryReq = {
    /** 记录日期（格式：yyyy-MM-dd） */
    date: string
  }

  type EntriesUpdateCheckedReq = {
    /** 记录ID */
    id: string
    /** 是否勾选 0-未勾选 1-已勾选 */
    checked?: number
  }

  type EntriesUpdateContentAndTypeReq = {
    /** 记录ID */
    id: string
    /** 记录内容 */
    content: string
    /** 记录类型 枚举值:Do/Idea/Think/Rule */
    entryType: string
  }

  type EntriesVo = {
    /** id */
    id?: string
    /** 用户ID */
    userId?: string
    /** 记录内容 */
    content?: string
    /** 记录类型 枚举值:Do/Idea/Think/Rule */
    entryType?: string
    /** 是否勾选 */
    checked?: number
    /** 创建时间 */
    createTime?: string
    /** 更新时间 */
    updateTime?: string
  }

  type getUserParams = {
    id: string
  }

  type getUserVOParams = {
    id: string
  }

  type HeatmapDataVo = {
    /** 日期（格式：yyyy-MM-dd） */
    date?: string
    /** 记录数量 */
    count?: number
  }

  type LoginUserVO = {
    id?: string
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
    updateTime?: string
  }

  type OrderItem = {
    column?: string
    asc?: boolean
  }

  type PageUserVO = {
    records?: UserVO[]
    total?: number
    size?: number
    current?: number
    orders?: OrderItem[]
    optimizeCountSql?: PageUserVO
    searchCount?: PageUserVO
    optimizeJoinOfCountSql?: boolean
    maxLimit?: number
    countId?: string
    pages?: number
  }

  type queryHeatmapDataParams = {
    year: string
  }

  type User = {
    id?: string
    userAccount?: string
    userPassword?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    editTime?: string
    createTime?: string
    updateTime?: string
    isDelete?: string
  }

  type UserAddRequest = {
    userName: string
    userAccount?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserLoginRequest = {
    userAccount: string
    userPassword: string
  }

  type UserQueryRequest = {
    pageNum?: number
    pageSize?: number
    sortField?: string
    sortOrder?: string
    id?: string
    userName?: string
    userAccount?: string
    userProfile?: string
    userRole?: string
  }

  type UserRegisterRequest = {
    userAccount: string
    userPassword: string
    checkPassword: string
  }

  type UserUpdateRequest = {
    id: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
  }

  type UserVO = {
    id?: string
    userAccount?: string
    userName?: string
    userAvatar?: string
    userProfile?: string
    userRole?: string
    createTime?: string
  }
}
