// src/App.tsx
import { useState, useEffect } from 'react';
import LotteryTable, {type LotteryData } from './components/LotteryTable';
import Header from './components/Header';
import Dashboard from './components/Dashboard';
import './App.css';

const API_BASE_URL = 'http://localhost:8080/api/lottery';

function App() {
    const [activeTab, setActiveTab] = useState<string>('home'); // Mặc định là 'home' để hiện dashboard
    const [selectedDate, setSelectedDate] = useState<string>(() => {
        // Set ngày mặc định là hôm nay
        const today = new Date();
        return today.toISOString().split('T')[0];
    });
    const [lotteryData, setLotteryData] = useState<LotteryData | null>(null);
    const [loading, setLoading] = useState<boolean>(false);
    const [error, setError] = useState<string | null>(null);

    // Fetch dữ liệu từ API khi selectedDate thay đổi (chỉ khi đang ở tab history)
    useEffect(() => {
        if (activeTab === 'history') {
            fetchLotteryData(selectedDate);
        }
    }, [selectedDate, activeTab]);

    const fetchLotteryData = async (date: string) => {
        setLoading(true);
        setError(null);
        
        try {
            const response = await fetch(`${API_BASE_URL}?date=${date}`);
            
            if (!response.ok) {
                if (response.status === 404) {
                    setError('Không tìm thấy dữ liệu cho ngày này');
                    setLotteryData(null);
                } else {
                    setError('Có lỗi xảy ra khi tải dữ liệu');
                    setLotteryData(null);
                }
                return;
            }
            
            const data: LotteryData = await response.json();
            setLotteryData(data);
        } catch (err) {
            console.error('Error fetching lottery data:', err);
            setError('Không thể kết nối đến server. Vui lòng kiểm tra lại.');
            setLotteryData(null);
        } finally {
            setLoading(false);
        }
    };

    const handleDateChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        setSelectedDate(e.target.value);
    };

    // Render nội dung theo activeTab
    const renderContent = () => {
        if (activeTab === 'home') {
            return <Dashboard />;
        } else if (activeTab === 'history') {
            return (
                <div className="search-container">
                    <div className="search-header">
                        <h2 className="search-title">
                            Tra Cứu Kết Quả Xổ Số
                        </h2>

                        <div className="search-date-wrapper">
                            <label className="search-date-label">
                                Chọn ngày xem:
                            </label>
                            <div className="search-date-input-wrapper">
                                <input
                                    type="date"
                                    value={selectedDate}
                                    onChange={handleDateChange}
                                    className="search-date-input"
                                />
                            </div>
                        </div>
                    </div>

                    <div className="search-content">
                        {loading && (
                            <div style={{ textAlign: 'center', padding: '40px', color: '#475569' }}>
                                <p>Đang tải dữ liệu...</p>
                            </div>
                        )}
                        
                        {error && (
                            <div style={{ 
                                textAlign: 'center', 
                                padding: '40px', 
                                color: '#ef4444',
                                background: 'rgba(239, 68, 68, 0.1)',
                                borderRadius: '12px',
                                margin: '20px 0'
                            }}>
                                <p>{error}</p>
                            </div>
                        )}
                        
                        {!loading && !error && lotteryData && (
                            <LotteryTable data={lotteryData} />
                        )}
                        
                        {!loading && !error && !lotteryData && (
                            <div style={{ textAlign: 'center', padding: '40px', color: '#64748b' }}>
                                <p>Chọn ngày để xem kết quả xổ số</p>
                            </div>
                        )}
                    </div>
                </div>
            );
        } else {
            // Các tab khác hiển thị màn hình trắng
            return (
                <div style={{ 
                    width: '100%', 
                    height: '100%', 
                    background: '#ffffff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center'
                }}>
                    {/* Màn hình trắng tạm thời */}
                </div>
            );
        }
    };

    return (
        <div style={{ display: 'flex', minHeight: '100vh', fontFamily: 'Arial', width: '100%', overflow: 'hidden' }}>
            <Header activeTab={activeTab} onTabChange={setActiveTab} />
            <main style={{ 
                marginLeft: '260px',
                padding: '40px',
                background: 'linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%)',
                height: '100vh',
                display: 'flex',
                alignItems: (activeTab === 'history' || activeTab === 'home') ? 'flex-start' : 'center',
                justifyContent: 'center',
                width: 'calc(100vw - 260px)',
                boxSizing: 'border-box',
                overflowX: 'hidden',
                overflowY: 'auto'
            }}>
                {renderContent()}
            </main>
        </div>
    );
}

export default App;