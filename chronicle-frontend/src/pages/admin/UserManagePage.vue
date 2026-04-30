<template>
  <div id="userManagePage">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-container">
        <div class="header-content">
          <h1 class="page-title">用户管理</h1>
          <p class="page-subtitle">管理系统中的所有用户</p>
        </div>
      </div>
    </div>

    <div class="container">
      <div class="content-card">
        <!-- 搜索表单 -->
        <div class="search-section">
          <van-form @submit="doSearch" class="search-form">
            <van-field
              v-model="searchParams.userAccount"
              name="userAccount"
              placeholder="输入账号"
              class="search-input"
            />
            <van-field
              v-model="searchParams.userName"
              name="userName"
              placeholder="输入用户名"
              class="search-input"
            />
            <div style="margin: 16px 0">
              <van-button round type="primary" native-type="submit" class="search-btn">
                <van-icon name="search" style="margin-right: 4px" />
                搜索
              </van-button>
            </div>
          </van-form>
        </div>

        <van-divider />

        <!-- 列表 -->
        <van-list :finished="finished" finished-text="没有更多了" @load="onLoad" class="user-list">
          <div v-for="item in data" :key="item.id" class="user-item">
            <div class="user-info">
              <van-image
                :src="item.userAvatar"
                width="48"
                height="48"
                fit="cover"
                class="user-avatar"
              />
              <div class="user-details">
                <div class="user-name">{{ item.userName }}</div>
                <div class="user-account">{{ item.userAccount }}</div>
                <van-tag v-if="item.userRole === 'admin'" type="primary" class="role-tag">
                  管理员
                </van-tag>
                <van-tag v-else type="success" class="role-tag"> 普通用户 </van-tag>
              </div>
            </div>
            <div class="user-meta">
              <div class="user-profile">{{ item.userProfile }}</div>
              <div class="time-text">
                {{ dayjs(item.createTime).format('YYYY-MM-DD HH:mm:ss') }}
              </div>
            </div>
            <div class="user-actions">
              <van-popconfirm
                title="确定要删除此用户吗?"
                confirm-button-text="确定"
                cancel-button-text="取消"
                @confirm="doDelete(item.id)"
              >
                <template #reference>
                  <van-button type="danger" size="small" class="delete-btn">删除</van-button>
                </template>
              </van-popconfirm>
            </div>
          </div>
        </van-list>
      </div>
    </div>
  </div>
</template>
<script lang="ts" setup>
import { onMounted, reactive, ref } from 'vue'
import { deleteUser, listUserVoByPage } from '@/api/userController.ts'
import { showToast } from 'vant'
import dayjs from 'dayjs'

// 展示的数据
const data = ref<API.UserVO[]>([])
const total = ref(0)
const finished = ref(false)

// 搜索条件
const searchParams = reactive<API.UserQueryRequest>({
  pageNum: 1,
  pageSize: 10,
})

// 获取数据
const fetchData = async () => {
  const res = await listUserVoByPage({
    ...searchParams,
  })
  if (res.data.data) {
    data.value = res.data.data.records ?? []
    total.value = res.data.data.total ?? 0
  } else {
    showToast({ type: 'fail', message: '获取数据失败，' + res.data.message })
  }
}

// 加载数据（用于 van-list）
const onLoad = async () => {
  await fetchData()
  finished.value = true
}

// 搜索数据
const doSearch = () => {
  // 重置页码
  searchParams.pageNum = 1
  finished.value = false
  data.value = []
  fetchData()
}

// 删除数据
const doDelete = async (id: string) => {
  if (!id) {
    return
  }
  const res = await deleteUser({ id })
  if (res.data.code === 0) {
    showToast({ type: 'success', message: '删除成功' })
    // 刷新数据
    fetchData()
  } else {
    showToast({ type: 'fail', message: '删除失败' })
  }
}

// 页面加载时请求一次
onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
#userManagePage {
  background: var(--color-background-secondary);
  min-height: 100vh;
  padding-bottom: 60px;

  .page-header {
    background: var(--gradient-hero);
    padding: 32px 20px;
    margin-bottom: 24px;
  }

  .header-container {
    max-width: 1200px;
    margin: 0 auto;
  }

  .header-content {
    color: var(--color-text);
  }

  .page-title {
    font-size: 28px;
    font-weight: 700;
    margin: 0 0 6px;
    letter-spacing: -0.5px;
    color: var(--color-text);
  }

  .page-subtitle {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin: 0;
  }

  .container {
    max-width: 1200px;
    margin: 0 auto;
    padding: 0 20px;
  }

  .content-card {
    border-radius: var(--radius-lg);
    border: 1px solid var(--color-border);
    box-shadow: none;
    background: white;
    padding: 24px;
  }

  .search-section {
    margin-bottom: 8px;
  }

  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    align-items: flex-end;
  }

  .search-input {
    width: 180px;
    border-radius: var(--radius-md);

    &:hover {
      border-color: var(--color-primary-light);
    }

    &:focus {
      border-color: var(--color-primary);
      box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.1);
    }
  }

  .search-btn {
    border-radius: var(--radius-md);
    font-weight: 500;
    background: var(--gradient-primary) !important;
    border: none !important;
    color: white !important;
    box-shadow: var(--shadow-green) !important;
    transition: opacity var(--transition-normal) !important;

    &:hover,
    &:focus,
    &:active {
      background: var(--gradient-primary) !important;
      border: none !important;
      color: white !important;
      box-shadow: var(--shadow-green) !important;
      opacity: 0.92;
    }
  }

  .user-list {
    margin-top: 16px;
  }

  .user-item {
    padding: 16px;
    border-bottom: 1px solid var(--color-border-light);
    transition: background var(--transition-fast);

    &:hover {
      background: rgba(34, 197, 94, 0.02);
    }
  }

  .user-info {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 8px;
  }

  .user-details {
    flex: 1;
  }

  .user-name {
    font-weight: 600;
    font-size: 14px;
    color: var(--color-text);
    margin-bottom: 4px;
  }

  .user-account {
    font-size: 13px;
    color: var(--color-text-secondary);
    margin-bottom: 6px;
  }

  .user-meta {
    margin-bottom: 8px;
  }

  .user-profile {
    font-size: 13px;
    color: var(--color-text-muted);
    margin-bottom: 4px;
  }

  .user-avatar {
    border: 2px solid var(--color-border);
  }

  .role-tag {
    border-radius: var(--radius-full);
    font-weight: 500;
    font-size: 12px;
  }

  .time-text {
    color: var(--color-text-secondary);
    font-size: 13px;
  }

  .delete-btn {
    font-weight: 500;
    font-size: 13px;
  }
}

@media (max-width: 768px) {
  #userManagePage {
    .page-header {
      padding: 24px 20px;
    }

    .page-title {
      font-size: 22px;
    }

    .search-form {
      flex-direction: column;
      align-items: stretch;
    }

    .search-input {
      width: 100%;
    }
  }
}
</style>
