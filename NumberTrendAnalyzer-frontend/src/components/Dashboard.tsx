// src/components/Dashboard.tsx
import React, { useState, useEffect } from 'react';
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

interface DashboardStats {
    totalDays: number;
    lastUpdateDate: string;
    topLoGan: LotoGan[];
    topLoHot: LotoHot[];
    quickForecast: QuickForecast;
}

const Dashboard: React.FC = () => {
    const [stats, setStats] = useState<DashboardStats | null>(null);
    const [loading, setLoading] = useState<boolean>(true);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        fetchDashboardStats();
    }, []);

    const fetchDashboardStats = async () => {
        setLoading(true);
        setError(null);
        
        try {
            const response = await fetch(`${API_BASE_URL}/stats`);
            
            if (!response.ok) {
                throw new Error('Không thể tải dữ liệu thống kê');
            }
            
            const data: DashboardStats = await response.json();
            setStats(data);
        } catch (err) {
            console.error('Error fetching dashboard stats:', err);
            setError('Không thể kết nối đến server. Vui lòng kiểm tra lại.');
        } finally {
            setLoading(false);
        }
    };

    const formatNumber = (num: number): string => {
        return num.toString().padStart(2, '0');
    };

    if (loading) {
        return (
            <div className="dashboard-container">
                <div className="dashboard-loading">
                    <p>Đang tải dữ liệu...</p>
                </div>
            </div>
        );
    }

    if (error) {
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
                            <h2 className="section-title">🔥 Top Lô Gan</h2>
                            <p className="section-subtitle">Các số lâu chưa về</p>
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
                            <h2 className="section-title">⭐ Top Lô Hot</h2>
                            <p className="section-subtitle">Về nhiều nhất trong 30 ngày</p>
                        </div>
                        <div className="lo-hot-grid">
                            {stats.topLoHot.length > 0 ? (
                                stats.topLoHot.map((item, index) => (
                                    <div key={item.number} className="lo-hot-card">
                                        <div className="lo-hot-number">{formatNumber(item.number)}</div>
                                        <div className="lo-hot-info">
                                            <div className="lo-hot-frequency">{item.frequency} lần</div>
                                            <div className="lo-hot-label">Trong 30 ngày qua</div>
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
                <div className="dashboard-section">
                    <div className="section-header section-header-purple">
                        <h2 className="section-title">🤖 Dự Báo Nhanh</h2>
                        <p className="section-subtitle">Dự đoán AI cho ngày mai</p>
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
                </div>
            </div>
        </div>
    );
};

export default Dashboard;

