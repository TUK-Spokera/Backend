const API_BASE_URL = window.location.origin; // 현재 호스트 기반으로 설정
const MATCH_REQUEST_URL = API_BASE_URL + `/api/match/request`
const CHAT_HISTORY_URL = API_BASE_URL + `/api/chat/history`
const CHAT_SOCKET_URL = API_BASE_URL + `/ws-chat`
const FACILITY_RECOMMEND_URL = API_BASE_URL + "/api/facility/recommend"
const MATCH_WAIT_LIST_URL = API_BASE_URL + "/api/match/wait-list"
const CHAT_ROOMS_URL = API_BASE_URL + "/api/chat/rooms"