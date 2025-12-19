// src/components/Dashboard.tsx
import React, { useState, useEffect, useRef } from 'react';
import './Dashboard.css';

const API_BASE_URL = 'http://localhost:8080/api/dashboard';

interface LotoGan {
    number: number;
    daysSinceLastAppearance: number;
    lastAppearanceDate: string;
}

interface LotoHot {
    number: number;
    frequency: number;
}

interface QuickForecast {
    rangeStart: number;
    rangeEnd: number;
    confidenceScore: number;
    algorithmUsed: string;
}

interface Recommendation {
    recommendedAlgorithm: string;
    recommendedRangeSize: number;
    recommendedRangeStart: number;
    recommendedRangeEnd: number;
    recommendedConfidenceScore: number;
    reason: string;
}

interface DashboardStats {
    totalDays: number;
    lastUpdateDate: string;
    topLoGan: LotoGan[];
    topLoHot: LotoHot[];
    quickForecast: QuickForecast;
    recommendation: Recommendation;
}

const Dashboard: React.FC = () => {
    const [stats, setStats] = useState<DashboardStats | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);
    const [loGanMonth, setLoGanMonth] = useState<string>(''); // Format: YYYY-MM
    const [loHotMonth, setLoHotMonth] = useState<string>(''); // Format: YYYY-MM
    const [selectedAlgorithm, setSelectedAlgorithm] = useState<string>('Frequency Analysis');
    const [rangeSize, setRangeSize] = useState<string>('20');
    const [rangeSizeError, setRangeSizeError] = useState<string>('');
    const forecastSectionRef = useRef<HTMLDivElement>(null);
    const isUserScrolledRef = useRef<boolean>(false);
    const isInitialLoadRef = useRef<boolean>(true);
    
    // Track xem người dùng có đang scroll không
    useEffect(() => {
        const handleScroll = () => {
            if (forecastSectionRef.current) {
                const forecastTop = forecastSectionRef.current.offsetTop;
                const scrollY = window.scrollY;
                const viewportHeight = window.innerHeight;
                
                // Nếu người dùng đã scroll xuống gần phần forecast (trong vòng 200px)
                if (scrollY + viewportHeight >= forecastTop - 200) {
                    isUserScrolledRef.current = true;
                }
            }
        };
        
        window.addEventListener('scroll', handleScroll);
        return () => window.removeEventListener('scroll', handleScroll);
    }, []);

    useEffect(() => {
        // Validate rangeSize trước khi fetch
        const rangeSizeNum = parseInt(rangeSize);
        const isValidRange = !isNaN(rangeSizeNum) && rangeSizeNum >= 10 && rangeSizeNum <= 60;
        
        // Chỉ fetch nếu rangeSize hợp lệ hoặc chưa nhập
        if (rangeSize === '' || isValidRange) {
            // Lưu vị trí scroll hiện tại TRƯỚC KHI fetch
            const savedScrollY = window.scrollY;
            const wasScrolledDown = savedScrollY > 200; // Nếu đã scroll xuống hơn 200px
            
            const fetchData = async () => {
                setLoading(true);
                setError(null);
                
                // Validate rangeSize trước khi fetch
                const num = parseInt(rangeSize);
                if (isNaN(num) || num < 10 || num > 60) {
                    setRangeSizeError('Khoảng số phải từ 10 đến 60');
                    setLoading(false);
                    return;
                } else {
                    setRangeSizeError('');
                }
                
                try {
                    // Tạo URL với các tham số
                    const params = new URLSearchParams();
                    if (loGanMonth) {
                        params.append('loGanMonth', loGanMonth);
                    }
                    if (loHotMonth) {
                        params.append('loHotMonth', loHotMonth);
                    }
                    if (selectedAlgorithm) {
                        params.append('algorithm', selectedAlgorithm);
                    }
                    if (rangeSize) {
                        params.append('rangeSize', rangeSize);
                    }
                    
                    const url = params.toString() 
                        ? `${API_BASE_URL}/stats?${params.toString()}`
                        : `${API_BASE_URL}/stats`;
                    
                    const response = await fetch(url);
                    
                    if (!response.ok) {
                        throw new Error('Không thể tải dữ liệu thống kê');
                    }
                    
                    const data: DashboardStats = await response.json();
                    setStats(data);
                    
                    // Sau khi set data, restore vị trí scroll
                    setTimeout(() => {
                        if (wasScrolledDown && forecastSectionRef.current) {
                            // Nếu người dùng đã scroll xuống, scroll đến forecast section
                            forecastSectionRef.current.scrollIntoView({ behavior: 'smooth', block: 'start' });
                        } else if (!wasScrolledDown && isInitialLoadRef.current) {
                            // Lần đầu load và đang ở đầu trang, giữ nguyên
                            window.scrollTo(0, 0);
                        }
                        // Nếu không phải lần đầu và không scroll xuống, không làm gì (giữ nguyên vị trí)
                    }, 150);
                    
                    // Đánh dấu đã load xong lần đầu
                    if (isInitialLoadRef.current) {
                        isInitialLoadRef.current = false;
                    }
                } catch (err) {
                    console.error('Error fetching dashboard stats:', err);
                    setError('Không thể kết nối đến server. Vui lòng kiểm tra lại.');
                } finally {
                    setLoading(false);
                }
            };
            
            fetchData();
        }
    }, [loGanMonth, loHotMonth, selectedAlgorithm, rangeSize]);
    
    // Tạo danh sách các tháng có thể chọn (12 tháng gần nhất)
    const getMonthOptions = (): string[] => {
        const options: string[] = ['']; // Thêm option "Tất cả"
        const today = new Date();
        for (let i = 0; i < 12; i++) {
            const date = new Date(today.getFullYear(), today.getMonth() - i, 1);
            const year = date.getFullYear();
            const month = String(date.getMonth() + 1).padStart(2, '0');
            options.push(`${year}-${month}`);
        }
        return options;
    };
    
    const formatMonthLabel = (monthStr: string): string => {
        if (!monthStr) return 'Tất cả';
        const [year, month] = monthStr.split('-');
        const monthNames = ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
                           'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'];
        return `${monthNames[parseInt(month) - 1]} ${year}`;
    };
    
    const handleLoGanMonthChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setLoGanMonth(e.target.value);
    };
    
    const handleLoHotMonthChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setLoHotMonth(e.target.value);
    };
    
    const handleRangeSizeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const value = e.target.value;
        setRangeSize(value);
        
        // Validate ngay khi nhập
        const numValue = parseInt(value);
        if (value === '') {
            setRangeSizeError('');
        } else if (isNaN(numValue)) {
            setRangeSizeError('Vui lòng nhập số hợp lệ');
        } else if (numValue < 10) {
            setRangeSizeError('Khoảng số tối thiểu là 10');
        } else if (numValue > 60) {
            setRangeSizeError('Khoảng số tối đa là 60');
        } else {
            setRangeSizeError('');
        }
    };
    
    const handleAlgorithmChange = (e: React.ChangeEvent<HTMLSelectElement>) => {
        setSelectedAlgorithm(e.target.value);
    };

    const formatNumber = (num: number): string => {
        return num.toString().padStart(2, '0');
    };
    
    // Tính toán text hiển thị cho dự báo dựa trên thời gian hiện tại
    const getForecastSubtitle = (): string => {
        const now = new Date();
        const currentHour = now.getHours();
        
        // Nếu thời gian hiện tại >= 19h (19:00) thì hiển thị "ngày mai"
        // Nếu thời gian hiện tại < 19h thì hiển thị "ngày hôm nay"
        if (currentHour >= 19) {
            return 'Dự đoán AI cho ngày mai';
        } else {
            return 'Dự đoán AI cho ngày hôm nay';
        }
    };

    if (loading && !stats) {
        // Chỉ hiển thị loading screen khi chưa có data (lần đầu load)
        return (
            <div className="dashboard-container">
                <div className="dashboard-loading">
                    <p>Đang tải dữ liệu...</p>
                </div>
            </div>
        );
    }

    if (error && !stats) {
        // Chỉ hiển thị error screen khi chưa có data
        return (
            <div className="dashboard-container">
                <div className="dashboard-error">
                    <p>{error}</p>
                </div>
            </div>
        );
    }

    if (!stats) {
        return (
            <div className="dashboard-container">
                <div className="dashboard-error">
                    <p>Không có dữ liệu</p>
                </div>
            </div>
        );
    }

    return (
        <div className="dashboard-container">
            {loading && (
                <div className="dashboard-loading-overlay">
                    <div className="dashboard-loading-spinner">
                        <p>Đang tải dữ liệu...</p>
                    </div>
                </div>
            )}
            <div className="dashboard-header">
                <h1 className="dashboard-title">Tổng Quan</h1>
                <p className="dashboard-subtitle">Thống kê và phân tích số liệu xổ số</p>
            </div>

            <div className="dashboard-content">
                {/* Thống kê nhanh */}
                <div className="stats-grid">
                    <div className="stat-card stat-card-primary">
                        <div className="stat-card-icon">📊</div>
                        <div className="stat-card-content">
                            <h3 className="stat-card-title">Tổng số ngày</h3>
                            <p className="stat-card-value">{stats.totalDays.toLocaleString('vi-VN')}</p>
                            <p className="stat-card-label">Đã có dữ liệu của {stats.totalDays.toLocaleString('vi-VN')} ngày</p>
                        </div>
                    </div>

                    <div className="stat-card stat-card-info">
                        <div className="stat-card-icon">🕒</div>
                        <div className="stat-card-content">
                            <h3 className="stat-card-title">Cập nhật gần nhất</h3>
                            <p className="stat-card-value">{stats.lastUpdateDate}</p>
                            <p className="stat-card-label">Ngày cập nhật dữ liệu mới nhất</p>
                        </div>
                    </div>
                </div>

                {/* Top Lô Gan và Lô Hot */}
                <div className="dashboard-sections">
                    {/* Top Lô Gan */}
                    <div className="dashboard-section">
                        <div className="section-header section-header-red">
                            <div className="section-header-content">
                                <div>
                                    <h2 className="section-title">🔥 Top Lô Gan</h2>
                                    <p className="section-subtitle">
                                        {loGanMonth ? `Các số chưa về từ ${formatMonthLabel(loGanMonth).toLowerCase()}` : 'Các số lâu chưa về'}
                                    </p>
                                </div>
                                <div className="section-filter">
                                    <label htmlFor="loGanMonth" className="filter-label">Lọc theo tháng:</label>
                                    <select 
                                        id="loGanMonth"
                                        value={loGanMonth} 
                                        onChange={handleLoGanMonthChange}
                                        className="filter-select"
                                    >
                                        {getMonthOptions().map(month => (
                                            <option key={month} value={month}>
                                                {formatMonthLabel(month)}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div className="lo-gan-grid">
                            {stats.topLoGan.length > 0 ? (
                                stats.topLoGan.map((item, index) => (
                                    <div key={item.number} className="lo-gan-card">
                                        <div className="lo-gan-number">{formatNumber(item.number)}</div>
                                        <div className="lo-gan-info">
                                            <div className="lo-gan-days">{item.daysSinceLastAppearance} ngày</div>
                                            <div className="lo-gan-date">Về: {item.lastAppearanceDate}</div>
                                        </div>
                                        <div className="lo-gan-rank">#{index + 1}</div>
                                    </div>
                                ))
                            ) : (
                                <p className="no-data">Chưa có dữ liệu</p>
                            )}
                        </div>
                    </div>

                    {/* Top Lô Hot */}
                    <div className="dashboard-section">
                        <div className="section-header section-header-blue">
                            <div className="section-header-content">
                                <div>
                                    <h2 className="section-title">⭐ Top Lô Hot</h2>
                                    <p className="section-subtitle">
                                        {loHotMonth ? `Về nhiều nhất trong ${formatMonthLabel(loHotMonth).toLowerCase()}` : 'Về nhiều nhất trong 30 ngày'}
                                    </p>
                                </div>
                                <div className="section-filter">
                                    <label htmlFor="loHotMonth" className="filter-label">Lọc theo tháng:</label>
                                    <select 
                                        id="loHotMonth"
                                        value={loHotMonth} 
                                        onChange={handleLoHotMonthChange}
                                        className="filter-select"
                                    >
                                        {getMonthOptions().map(month => (
                                            <option key={month} value={month}>
                                                {formatMonthLabel(month)}
                                            </option>
                                        ))}
                                    </select>
                                </div>
                            </div>
                        </div>
                        <div className="lo-hot-grid">
                            {stats.topLoHot.length > 0 ? (
                                stats.topLoHot.map((item, index) => (
                                    <div key={item.number} className="lo-hot-card">
                                        <div className="lo-hot-number">{formatNumber(item.number)}</div>
                                        <div className="lo-hot-info">
                                            <div className="lo-hot-frequency">{item.frequency} lần</div>
                                            <div className="lo-hot-label">
                                                {loHotMonth ? `Trong ${formatMonthLabel(loHotMonth).toLowerCase()}` : 'Trong 30 ngày qua'}
                                            </div>
                                        </div>
                                        <div className="lo-hot-rank">#{index + 1}</div>
                                    </div>
                                ))
                            ) : (
                                <p className="no-data">Chưa có dữ liệu</p>
                            )}
                        </div>
                    </div>
                </div>

                {/* Dự báo nhanh */}
                <div className="dashboard-section" ref={forecastSectionRef}>
                    <div className="section-header section-header-purple">
                        <div className="section-header-content">
                            <div>
                                <h2 className="section-title">🤖 Dự Báo Nhanh</h2>
                                <p className="section-subtitle">{getForecastSubtitle()}</p>
                            </div>
                            <div className="forecast-controls">
                                <div className="forecast-control-group">
                                    <label htmlFor="algorithm" className="filter-label">Thuật toán:</label>
                                    <select 
                                        id="algorithm"
                                        value={selectedAlgorithm} 
                                        onChange={handleAlgorithmChange}
                                        className="filter-select"
                                    >
                                        <option value="Frequency Analysis">Frequency Analysis</option>
                                        <option value="Long Short-Term Memory">Long Short-Term Memory</option>
                                        <option value="Markov Chains">Markov Chains</option>
                                    </select>
                                </div>
                                <div className="forecast-control-group">
                                    <label htmlFor="rangeSize" className="filter-label">Khoảng số:</label>
                                    <input
                                        id="rangeSize"
                                        type="number"
                                        min="10"
                                        max="60"
                                        value={rangeSize}
                                        onChange={handleRangeSizeChange}
                                        className={`filter-input ${rangeSizeError ? 'input-error' : ''}`}
                                        placeholder="Nhập 10-60"
                                    />
                                    {rangeSizeError && (
                                        <span className="error-message">{rangeSizeError}</span>
                                    )}
                                </div>
                            </div>
                        </div>
                    </div>
                    <div className="forecast-card">
                        <div className="forecast-range">
                            <span className="forecast-range-label">Khả năng cao rơi vào khoảng:</span>
                            <span className="forecast-range-value">
                                {stats.quickForecast.rangeStart} - {stats.quickForecast.rangeEnd}
                            </span>
                        </div>
                        <div className="forecast-details">
                            <div className="forecast-confidence">
                                <span className="forecast-label">Độ tin cậy:</span>
                                <span className="forecast-value">
                                    {(stats.quickForecast.confidenceScore * 100).toFixed(0)}%
                                </span>
                            </div>
                            <div className="forecast-algorithm">
                                <span className="forecast-label">Thuật toán:</span>
                                <span className="forecast-value">{stats.quickForecast.algorithmUsed}</span>
                            </div>
                        </div>
                    </div>
                    
                    {/* Gợi ý dựa trên tổng hợp 3 thuật toán */}
                    {stats.recommendation && (
                        <div className="recommendation-card">
                            <div className="recommendation-header">
                                <span className="recommendation-icon">💡</span>
                                <span className="recommendation-title">Gợi ý tối ưu</span>
                            </div>
                            <div className="recommendation-content">
                                <p className="recommendation-text">
                                    {stats.recommendation.reason}
                                </p>
                                <div className="recommendation-details">
                                    <div className="recommendation-item">
                                        <span className="recommendation-label">Thuật toán:</span>
                                        <span className="recommendation-value">{stats.recommendation.recommendedAlgorithm}</span>
                                    </div>
                                    <div className="recommendation-item">
                                        <span className="recommendation-label">Khoảng số:</span>
                                        <span className="recommendation-value">{stats.recommendation.recommendedRangeSize}</span>
                                    </div>
                                    <div className="recommendation-item">
                                        <span className="recommendation-label">Dự đoán:</span>
                                        <span className="recommendation-value">
                                            {stats.recommendation.recommendedRangeStart} - {stats.recommendation.recommendedRangeEnd}
                                        </span>
                                    </div>
                                    <div className="recommendation-item">
                                        <span className="recommendation-label">Độ tin cậy:</span>
                                        <span className="recommendation-value">
                                            {(stats.recommendation.recommendedConfidenceScore * 100).toFixed(0)}%
                                        </span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Dashboard;

