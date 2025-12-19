// src/App.tsx
import { useState, useEffect } from 'react';
import LotteryTable, {type LotteryData } from './components/LotteryTable';
import Header from './components/Header';
import Dashboard from './components/Dashboard';
import Analysis from './components/Analysis';
import EvenOddAnalysis from './components/EvenOddAnalysis';
import PrimeAnalysis from './components/PrimeAnalysis';
import Divide3Analysis from './components/Divide3Analysis';
import AnalysisSelector from './components/AnalysisSelector';
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
    const [updating, setUpdating] = useState<boolean>(false);
    const [updateMessage, setUpdateMessage] = useState<string | null>(null);
    const [selectedAlgorithm, setSelectedAlgorithm] = useState<string | null>(null);

    // Fetch dữ liệu từ API khi selectedDate thay đổi (chỉ khi đang ở tab history)
    useEffect(() => {
        if (activeTab === 'history') {
            fetchLotteryData(selectedDate);
        }
    }, [selectedDate, activeTab]);

    // Reset selectedAlgorithm khi chuyển sang tab khác
    useEffect(() => {
        if (activeTab !== 'analysis') {
            setSelectedAlgorithm(null);
        }
    }, [activeTab]);

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

    const handleAutoUpdate = async () => {
        setUpdating(true);
        setUpdateMessage(null);
        setError(null);
        
        try {
            const response = await fetch('http://localhost:8080/api/crawl/auto-update');
            
            if (!response.ok) {
                throw new Error('Có lỗi xảy ra khi cập nhật dữ liệu');
            }
            
            const result = await response.text();
            setUpdateMessage(result);
            
            // Sau khi cập nhật thành công, refresh dữ liệu hiện tại nếu đang xem ngày hôm nay
            const today = new Date().toISOString().split('T')[0];
            if (selectedDate === today) {
                fetchLotteryData(selectedDate);
            }
        } catch (err) {
            console.error('Error updating data:', err);
            setError('Không thể kết nối đến server để cập nhật dữ liệu.');
        } finally {
            setUpdating(false);
        }
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
                            <button
                                onClick={handleAutoUpdate}
                                disabled={updating}
                                className="update-button"
                                style={{
                                    marginLeft: '16px',
                                    padding: '10px 20px',
                                    backgroundColor: updating ? '#94a3b8' : '#3b82f6',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '8px',
                                    cursor: updating ? 'not-allowed' : 'pointer',
                                    fontSize: '14px',
                                    fontWeight: '500',
                                    transition: 'background-color 0.2s'
                                }}
                            >
                                {updating ? 'Đang cập nhật...' : '🔄 Cập nhật dữ liệu'}
                            </button>
                        </div>
                    </div>

                    <div className="search-content">
                        {loading && (
                            <div style={{ textAlign: 'center', padding: '40px', color: '#94a3b8' }}>
                                <p>Đang tải dữ liệu...</p>
                            </div>
                        )}
                        
                        {updateMessage && (
                            <div style={{ 
                                textAlign: 'center', 
                                padding: '20px', 
                                color: '#34d399',
                                background: 'rgba(5, 150, 105, 0.15)',
                                border: '1px solid rgba(5, 150, 105, 0.3)',
                                borderRadius: '12px',
                                margin: '20px 0',
                                fontSize: '14px',
                                fontWeight: '500'
                            }}>
                                <p>{updateMessage}</p>
                            </div>
                        )}
                        
                        {error && (
                            <div style={{ 
                                textAlign: 'center', 
                                padding: '40px', 
                                color: '#f87171',
                                background: 'rgba(239, 68, 68, 0.15)',
                                border: '1px solid rgba(239, 68, 68, 0.3)',
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
        } else if (activeTab === 'analysis') {
            // Nếu chưa chọn thuật toán, hiển thị màn hình chọn
            if (!selectedAlgorithm) {
                return (
                    <AnalysisSelector 
                        onSelectAlgorithm={(algorithm) => {
                            if (algorithm === '50-50' || algorithm === 'even-odd' || algorithm === 'prime' || algorithm === 'divide-3') {
                                setSelectedAlgorithm(algorithm);
                            } else {
                                alert('Chức năng đang phát triển');
                            }
                        }} 
                    />
                );
            }
            
            // Nếu đã chọn thuật toán 50-50, hiển thị giao diện phân tích
            if (selectedAlgorithm === '50-50') {
                return <Analysis onBack={() => setSelectedAlgorithm(null)} />;
            }
            
            // Nếu đã chọn thuật toán even-odd, hiển thị giao diện phân tích chẵn lẻ
            if (selectedAlgorithm === 'even-odd') {
                return <EvenOddAnalysis onBack={() => setSelectedAlgorithm(null)} />;
            }
            
            // Nếu đã chọn thuật toán prime, hiển thị giao diện phân tích số nguyên tố
            if (selectedAlgorithm === 'prime') {
                return <PrimeAnalysis onBack={() => setSelectedAlgorithm(null)} />;
            }
            
            // Nếu đã chọn thuật toán divide-3, hiển thị giao diện phân tích chia 3
            if (selectedAlgorithm === 'divide-3') {
                return <Divide3Analysis onBack={() => setSelectedAlgorithm(null)} />;
            }
            
            // Các thuật toán khác (chưa phát triển)
            return (
                <div style={{ 
                    width: '100%', 
                    height: '100%', 
                    background: 'transparent',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#e2e8f0'
                }}>
                    <div style={{ textAlign: 'center' }}>
                        <h2 style={{ fontSize: '24px', marginBottom: '16px' }}>Chức năng đang phát triển</h2>
                        <button
                            onClick={() => setSelectedAlgorithm(null)}
                            style={{
                                padding: '10px 20px',
                                backgroundColor: '#3b82f6',
                                color: 'white',
                                border: 'none',
                                borderRadius: '8px',
                                cursor: 'pointer',
                                fontSize: '14px',
                                fontWeight: '500'
                            }}
                        >
                            Quay lại
                        </button>
                    </div>
                </div>
            );
        } else {
            // Các tab khác hiển thị màn hình dark
            return (
                <div style={{ 
                    width: '100%', 
                    height: '100%', 
                    background: 'transparent',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#e2e8f0'
                }}>
                    {/* Màn hình tạm thời */}
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
                background: 'linear-gradient(135deg, #0f172a 0%, #1a202c 100%)',
                height: '100vh',
                display: 'flex',
                alignItems: (activeTab === 'history' || activeTab === 'home' || (activeTab === 'analysis' && (selectedAlgorithm === '50-50' || selectedAlgorithm === 'even-odd' || selectedAlgorithm === 'prime' || selectedAlgorithm === 'divide-3'))) ? 'flex-start' : 'center',
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