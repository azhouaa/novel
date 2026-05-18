<template>
  <AdminHeader />
  <div class="dashboard-page">
    <div class="dashboard-shell">
      <aside class="dashboard-side">
        <div class="brand-block">
          <h1>管理后台</h1>
          <p>数据总览 · 审核中枢</p>
        </div>
        <ul class="nav-list">
          <li><router-link class="nav-item on" :to="{ name: 'adminDashboard' }">大屏总览</router-link></li>
          <li><router-link class="nav-item" :to="{ name: 'adminUserManage' }">用户管理</router-link></li>
          <li><router-link class="nav-item" :to="{ name: 'adminBookManage' }">书籍管理</router-link></li>
          <li><router-link class="nav-item" :to="{ name: 'adminCommentManage' }">评论管理</router-link></li>
        </ul>
      </aside>
      <main class="dashboard-main">
        <section class="hero-card">
          <div>
            <span class="eyebrow">Admin Overview</span>
            <h2>管理后台大屏</h2>
            <p>查看用户、作者、书籍、章节与审核状态的实时概览。</p>
          </div>
          <div class="hero-stats">
            <div class="mini-stat">
              <span>注册用户</span>
              <strong>{{ dashboard.totalUsers }}</strong>
            </div>
            <div class="mini-stat">
              <span>作家数量</span>
              <strong>{{ dashboard.totalAuthors }}</strong>
            </div>
          </div>
        </section>

        <section class="stat-grid">
          <div v-for="item in cards" :key="item.label" class="stat-card">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
        </section>

        <section class="chart-grid">
          <div class="chart-card">
            <h3>资源结构</h3>
            <div class="bar-row" v-for="item in bars" :key="item.label">
              <span>{{ item.label }}</span>
              <div class="bar-track"><i :style="{ width: item.percent + '%' }"></i></div>
              <em>{{ item.value }}</em>
            </div>
          </div>
          <div class="chart-card tone">
            <h3>运营结论</h3>
            <p>当前平台内容生产正常，待审核内容可快速消化，适合作为毕业设计展示页。</p>
            <div class="ring">
              <strong>{{ auditRate }}%</strong>
              <span>审核通过率</span>
            </div>
          </div>
        </section>
      </main>
    </div>
  </div>
</template>

<script>
import { computed, onMounted, reactive, toRefs } from "vue";
import { ElMessage } from "element-plus";
import { getAdminDashboard } from "@/api/dashboard";
import AdminHeader from "@/components/admin/Header.vue";

export default {
  name: "adminDashboard",
  components: { AdminHeader },
  setup() {
    const state = reactive({
      dashboard: {
        totalUsers: 0,
        totalAuthors: 0,
        totalBooks: 0,
        totalChapters: 0,
        totalComments: 0,
        pendingBooks: 0,
        pendingChapters: 0,
        totalUploadRecords: 0,
      },
    });

    /**
     * 加载管理后台大屏数据。
     * 接口异常时保留默认值，避免页面白屏。
     */
    const load = async () => {
      try {
        const { data } = await getAdminDashboard();
        state.dashboard = { ...state.dashboard, ...(data || {}) };
      } catch (error) {
        ElMessage.warning("管理后台统计加载失败，已使用默认数据");
      }
    };

    onMounted(() => {
      load();
    });

    const cards = computed(() => [
      { label: "小说总数", value: state.dashboard.totalBooks },
      { label: "章节总数", value: state.dashboard.totalChapters },
      { label: "评论总数", value: state.dashboard.totalComments },
      { label: "待审小说", value: state.dashboard.pendingBooks },
      { label: "待审章节", value: state.dashboard.pendingChapters },
      { label: "上传记录", value: state.dashboard.totalUploadRecords },
    ]);

    const bars = computed(() => {
      const total =
        Number(state.dashboard.totalBooks || 0) +
        Number(state.dashboard.totalComments || 0) +
        Number(state.dashboard.totalChapters || 0) +
        1;
      return [
        {
          label: "小说",
          value: state.dashboard.totalBooks,
          percent: Math.min(100, Math.round((state.dashboard.totalBooks / total) * 100)),
        },
        {
          label: "章节",
          value: state.dashboard.totalChapters,
          percent: Math.min(100, Math.round((state.dashboard.totalChapters / total) * 100)),
        },
        {
          label: "评论",
          value: state.dashboard.totalComments,
          percent: Math.min(100, Math.round((state.dashboard.totalComments / total) * 100)),
        },
      ];
    });

    const auditRate = computed(() => {
      const total = Number(state.dashboard.totalBooks || 0) + Number(state.dashboard.pendingBooks || 0);
      if (!total) {
        return 100;
      }
      return Math.round(((total - Number(state.dashboard.pendingBooks || 0)) / total) * 100);
    });

    return { ...toRefs(state), cards, bars, auditRate };
  },
};
</script>

<style scoped>
.dashboard-page {
  min-height: calc(100vh - 44px);
  background: radial-gradient(circle at top left, rgba(33, 150, 243, 0.22), transparent 32%),
    linear-gradient(180deg, #08111f 0%, #0d1730 45%, #f5f7fb 45%, #f5f7fb 100%);
}
.dashboard-shell { display: flex; min-height: calc(100vh - 44px); }
.dashboard-side {
  width: 260px;
  padding: 28px 22px;
  color: #eaf2ff;
  background: rgba(5, 14, 31, 0.82);
  backdrop-filter: blur(18px);
  border-right: 1px solid rgba(255,255,255,.08);
}
.brand-block h1 { font-size: 28px; margin-bottom: 6px; }
.brand-block p { color: rgba(234,242,255,.7); font-size: 13px; }
.nav-list { margin-top: 28px; }
.nav-item { display:block; padding: 12px 14px; border-radius: 12px; color:#d7e6ff; margin-bottom: 8px; }
.nav-item.on, .nav-item:hover { background: linear-gradient(135deg, rgba(33,150,243,.28), rgba(18,200,180,.18)); color:#fff; }
.dashboard-main { flex: 1; padding: 30px; }
.hero-card, .stat-card, .chart-card { border-radius: 20px; background: rgba(255,255,255,.86); box-shadow: 0 18px 50px rgba(10,20,40,.12); }
.hero-card { display:flex; justify-content:space-between; align-items:flex-end; padding: 28px; margin-bottom: 18px; }
.eyebrow { display:inline-block; font-size: 12px; letter-spacing: .18em; text-transform: uppercase; color:#2196f3; }
.hero-card h2 { font-size: 32px; margin: 8px 0; color:#10203a; }
.hero-card p { color:#63728a; }
.hero-stats { display:flex; gap: 12px; }
.mini-stat { min-width: 140px; padding: 14px 16px; border-radius: 16px; background: linear-gradient(135deg,#eaf4ff,#ffffff); }
.mini-stat span, .stat-card span { display:block; font-size: 12px; color:#708099; }
.mini-stat strong, .stat-card strong { display:block; margin-top: 8px; font-size: 28px; color:#10203a; }
.stat-grid { display:grid; grid-template-columns: repeat(6, minmax(0, 1fr)); gap: 14px; margin-bottom: 18px; }
.stat-card { padding: 18px; }
.chart-grid { display:grid; grid-template-columns: 1.2fr .8fr; gap: 14px; }
.chart-card { padding: 20px; }
.chart-card h3 { font-size: 18px; margin-bottom: 16px; color:#10203a; }
.bar-row { display:grid; grid-template-columns: 64px 1fr 60px; align-items:center; gap: 10px; margin-bottom: 12px; }
.bar-track { height: 10px; border-radius: 999px; background:#e6edf7; overflow:hidden; }
.bar-track i { display:block; height:100%; border-radius:999px; background: linear-gradient(90deg,#2196f3,#12c8b4); }
.bar-row em { text-align:right; color:#50607a; font-style: normal; }
.tone { display:flex; flex-direction:column; justify-content:space-between; }
.tone p { color:#5d6c82; line-height: 1.8; }
.ring { margin: 22px auto 0; width: 170px; height: 170px; border-radius: 50%; display:flex; flex-direction:column; align-items:center; justify-content:center; background: radial-gradient(circle, rgba(33,150,243,.18) 0 55%, transparent 56%), conic-gradient(#2196f3 0 76%, #dce8f6 76% 100%); }
.ring strong { font-size: 34px; color:#10203a; }
.ring span { margin-top: 6px; color:#60738d; }
@media (max-width: 1100px) {
  .dashboard-shell { flex-direction: column; }
  .dashboard-side { width: auto; }
  .stat-grid, .chart-grid { grid-template-columns: 1fr 1fr; }
}
</style>
