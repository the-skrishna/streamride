import React from 'react';
import '../styles/dashboard.css';

interface MetricsCardProps {
    title: string;
    value: string | number;
    subtitle?: string;
    icon?: string;
}

export const MetricsCard: React.FC<MetricsCardProps> = ({
                                                            title,
                                                            value,
                                                            subtitle,
                                                            icon,
                                                        }) => {
    return (
        <div className="metrics-card">
            <div className="metrics-card-header">
                {icon && <span className="metrics-icon">{icon}</span>}
                <h3>{title}</h3>
            </div>
            <div className="metrics-card-value">{value}</div>
            {subtitle && <div className="metrics-card-subtitle">{subtitle}</div>}
        </div>
    );
};