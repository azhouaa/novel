<template>
  <Header />

  <div class="main box_center cf">
    <div class="userBox user-center-wide cf">
      <UserMenu />
      <div class="my_r">
        <div class="shelf-panel">
          <div class="shelf-header">
            <div>
              <h2>我的书架</h2>
              <p>共 {{ books.length }} 本，支持续读与移除管理。</p>
            </div>
            <a href="javascript:void(0)" class="refresh-btn" @click="loadBookshelf">
              {{ loading ? '刷新中...' : '刷新书架' }}
            </a>
          </div>

          <div v-if="!loading && books.length === 0" class="no_contet empty-shelf">
            <p>你的书架还是空的，去挑一本开始阅读吧。</p>
          </div>

          <div v-if="books.length > 0" class="shelf-list">
            <div class="shelf-item" v-for="item in books" :key="item.bookId">
              <div class="book-main">
                <a href="javascript:void(0)" class="book-name" @click="continueRead(item)">{{ item.bookName }}</a>
                <span class="author">{{ item.authorName }}</span>
              </div>
              <div class="book-progress">
                <p>阅读到：{{ item.lastReadChapterName || '暂未记录章节' }}</p>
                <p>最新章：{{ item.lastChapterName || '暂无更新' }}</p>
              </div>
              <div class="book-meta">更新：{{ item.updateTime || '--' }}</div>
              <div class="book-actions">
                <a href="javascript:void(0)" class="link-primary" @click="continueRead(item)">继续阅读</a>
                <a href="javascript:void(0)" class="link-danger" @click="removeBook(item.bookId)">移出书架</a>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  <Footer />
</template>

<script>
import "@/assets/styles/user.css";
import { reactive, toRefs, onMounted } from "vue";
import { useRouter } from "vue-router";
import {
  getBookshelfReadChapterId,
  listBookshelf,
  removeFromBookshelf,
} from "@/api/user";
import { ElMessage, ElMessageBox } from "element-plus";
import Header from "@/components/common/Header";
import Footer from "@/components/common/Footer";
import UserMenu from "@/components/user/Menu";

export default {
  name: "userBookshelf",
  components: {
    Header,
    Footer,
    UserMenu,
  },
  setup() {
    const router = useRouter();
    const state = reactive({
      books: [],
      loading: false,
    });

    onMounted(() => {
      loadBookshelf();
    });

    /**
     * 加载书架列表并同步章节进度信息。
     */
    const loadBookshelf = async () => {
      if (state.loading) {
        return;
      }
      state.loading = true;
      try {
        const { data } = await listBookshelf();
        state.books = data || [];
      } finally {
        state.loading = false;
      }
    };

    /**
     * 续读优先使用列表中的续读章节，不存在时再调用接口兜底。
     */
    const continueRead = async (item) => {
      let chapterId = item.continueChapterId;
      if (!chapterId) {
        const { data } = await getBookshelfReadChapterId(item.bookId);
        chapterId = data;
      }
      router.push({ path: `/book/${item.bookId}/${chapterId}` });
    };

    /**
     * 从书架删除一本书，保留二次确认避免误删。
     */
    const removeBook = async (bookId) => {
      await ElMessageBox.confirm("确定将这本书从书架移除吗？", "提示", {
        confirmButtonText: "确定",
        cancelButtonText: "取消",
        type: "warning",
      });
      await removeFromBookshelf(bookId);
      ElMessage.success("已移出书架");
      await loadBookshelf();
    };

    return {
      ...toRefs(state),
      continueRead,
      removeBook,
      loadBookshelf,
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

.shelf-panel {
  border: 1px solid #e7edf5;
  border-radius: 10px;
  background: linear-gradient(180deg, #ffffff 0%, #f7fbff 100%);
  padding: 24px;
}

.shelf-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid #e8eef7;
  padding-bottom: 14px;
  margin-bottom: 16px;
}

.shelf-header h2 {
  font-size: 24px;
  color: #1f2f46;
  margin-bottom: 6px;
}

.shelf-header p {
  color: #7a8aa2;
  font-size: 14px;
}

.refresh-btn {
  display: inline-block;
  padding: 8px 14px;
  border-radius: 18px;
  border: 1px solid #d5e4f6;
  color: #4b6f99;
  background: #fff;
}

.refresh-btn:hover {
  border-color: #8bb4de;
  color: #2f628f;
}

.empty-shelf {
  border-top: 0;
  background: #fff;
  border: 1px dashed #d6e3f3;
  border-radius: 8px;
  padding: 26px;
  color: #6c7f99;
}

.shelf-list {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
}

.shelf-item {
  display: grid;
  grid-template-columns: 1.2fr 1.3fr 0.8fr 0.8fr;
  align-items: center;
  gap: 12px;
  border: 1px solid #e4ebf5;
  border-radius: 8px;
  background: #fff;
  padding: 14px 16px;
}

.book-main {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.book-name {
  font-size: 17px;
  color: #253b58;
  font-weight: 600;
}

.book-name:hover {
  color: #2196f3;
}

.author {
  color: #7e8ea6;
}

.book-progress p {
  color: #5f7390;
  line-height: 1.8;
}

.book-meta {
  color: #8594ab;
}

.book-actions {
  display: flex;
  gap: 14px;
  justify-content: flex-end;
}

.link-primary {
  color: #2196f3;
}

.link-danger {
  color: #df5d61;
}

@media (max-width: 960px) {
  .shelf-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .shelf-item {
    grid-template-columns: 1fr;
    gap: 10px;
  }

  .book-actions {
    justify-content: flex-start;
  }
}
</style>
