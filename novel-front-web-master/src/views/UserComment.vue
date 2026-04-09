<template>
  <Header />

  <div class="main box_center cf">
    <div class="userBox user-center-wide cf">
      <UserMenu />
      <div class="my_r">
        <div class="comment-panel">
          <div class="comment-header">
            <h2>我的评论</h2>
            <p>共 {{ total }} 条评论，记录你在书籍页留下的阅读观点。</p>
          </div>

          <div v-if="!loading && total === 0" class="no_contet empty-comment">
            你还没有发布过评论，去小说详情页写下第一条吧。
          </div>

          <div v-else class="comment-list">
            <div class="comment-item" v-for="(item, index) in comments" :key="index">
              <div class="book-cover">
                <img :src="imgBaseUrl + item.commentBookPic" :alt="item.commentBook" />
              </div>
              <div class="comment-body">
                <h3 class="book-name">《{{ item.commentBook || '未知书籍' }}》</h3>
                <p class="comment-content" v-html="item.commentContent"></p>
                <p class="comment-time">评论时间：{{ item.commentTime }}</p>
              </div>
            </div>
          </div>

          <el-pagination
            v-if="total > 0"
            small
            layout="prev, pager, next"
            :page-size="pageSize"
            :total="total"
            class="mt-4 comment-pagination"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </div>
  </div>
  <Footer />
</template>

<script>
import "@/assets/styles/user.css";
import { listComments } from "@/api/user";
import { reactive, toRefs, onMounted } from "vue";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
import UserMenu from "@/components/user/Menu";

export default {
  name: "userComment",
  components: {
    Header,
    Footer,
    UserMenu,
  },
  setup() {
    const state = reactive({
      total: 0,
      loading: false,
      pageSize: 10,
      comments: [],
      imgBaseUrl: process.env.VUE_APP_BASE_IMG_URL,
    });

    onMounted(async () => {
      await loadComments(1);
    });

    /**
     * 切换分页时，按页码拉取评论数据。
     */
    const handleCurrentChange = async (pageNum) => {
      await loadComments(pageNum);
    };

    /**
     * 加载我的评论列表，并同步总条数给分页组件。
     */
    const loadComments = async (pageNum) => {
      if (state.loading) {
        return;
      }
      state.loading = true;
      try {
        const { data } = await listComments({ pageNum, pageSize: state.pageSize });
        state.comments = data?.list || [];
        state.total = Number(data?.total || 0);
      } finally {
        state.loading = false;
      }
    };

    return {
      ...toRefs(state),
      handleCurrentChange,
    };
  },
};
</script>

<style scoped>
.user-center-wide {
  padding: 0 24px 26px;
}

.user-center-wide .my_r {
  width: 100%;
  float: none;
  border-left: 0;
  padding: 0;
  min-height: auto;
}

.comment-panel {
  border: 1px solid #e7edf5;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  padding: 24px;
}

.comment-header {
  margin-bottom: 16px;
  border-bottom: 1px solid #e8eef7;
  padding-bottom: 14px;
}

.comment-header h2 {
  font-size: 24px;
  color: #1f2f46;
  margin-bottom: 6px;
}

.comment-header p {
  color: #7a8aa2;
  font-size: 14px;
}

.empty-comment {
  border-top: 0;
  background: #fff;
  border: 1px dashed #d6e3f3;
  border-radius: 8px;
  padding: 26px;
  color: #6c7f99;
}

.comment-list {
  display: grid;
  gap: 12px;
}

.comment-item {
  display: grid;
  grid-template-columns: 74px 1fr;
  gap: 14px;
  border: 1px solid #e4ebf5;
  border-radius: 8px;
  background: #fff;
  padding: 14px;
}

.book-cover img {
  width: 64px;
  height: 84px;
  object-fit: cover;
  border-radius: 4px;
  border: 1px solid #e7edf6;
}

.comment-body {
  min-width: 0;
}

.book-name {
  color: #253b58;
  font-size: 16px;
  margin-bottom: 8px;
}

.comment-content {
  color: #566d8d;
  line-height: 1.8;
  margin-bottom: 10px;
}

.comment-time {
  color: #8a98ad;
  font-size: 13px;
}

.comment-pagination {
  margin-top: 18px;
  justify-content: center;
}

.comment-pagination :deep(.is-active) {
  background-color: #2196f3 !important;
}

.comment-pagination :deep(li:hover) {
  color: #2196f3 !important;
}

@media (max-width: 768px) {
  .comment-item {
    grid-template-columns: 1fr;
  }
}
</style>
