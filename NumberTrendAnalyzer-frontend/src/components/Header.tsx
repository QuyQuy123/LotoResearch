import React, { useState } from 'react';
import { FaChartBar, FaDatabase, FaBrain, FaTable, FaHome } from 'react-icons/fa'; // Icon
import './Header.css';

const Header: React.FC = () => {
    const [activeTab, setActiveTab] = useState('home');
    return (
        <header className="header-container">
            {/* 1. Logo / Tên Dự Án */}
            <div className="logo-section">
                <div className="logo-icon">🎱</div>
                <div className="logo-text">
                    <h1>LotoResearch</h1>
                    <span>AI & Data Analysis</span>
                </div>
            </div>

            {/* 2. Thanh Menu chính */}
            <nav className="nav-menu">
                <ul className="nav-list">
                    <li
                        className={activeTab === 'home' ? 'active' : ''}
                        onClick={() => setActiveTab('home')}
                    >
                        <FaHome className="icon" /> <span>Tổng Quan</span>
                    </li>

                    <li
                        className={activeTab === 'history' ? 'active' : ''}
                        onClick={() => setActiveTab('history')}
                    >
                        <FaTable className="icon" /> <span>Sổ Kết Quả</span>
                    </li>

                    <li
                        className={activeTab === 'analysis' ? 'active' : ''}
                        onClick={() => setActiveTab('analysis')}
                    >
                        <FaChartBar className="icon" /> <span>Phân Tích</span>
                    </li>

                    <li
                        className={activeTab === 'predict' ? 'active' : ''}
                        onClick={() => setActiveTab('predict')}
                    >
                        <FaBrain className="icon" /> <span>Dự Báo (AI)</span>
                    </li>

                    <li
                        className={activeTab === 'data' ? 'active' : ''}
                        onClick={() => setActiveTab('data')}
                    >
                        <FaDatabase className="icon" /> <span>Dữ Liệu</span>
                    </li>
                </ul>
            </nav>

            {/* 3. Phần User / Cấu hình nhỏ bên dưới */}
            <div className="user-actions">
                <button className="btn-login">Admin</button>
            </div>
        </header>
    );
};

export default Header;