import React, { useEffect, useState } from 'react';
import { useWebSocket } from '../hooks/useWebSocket';
import { useMetrics } from '../hooks/useMetrics';
import type { DashboardMetrics } from '../types/metrics';
import { MetricsCard } from './MetricsCard';
import { TopCitiesChart } from './TopCitiesChart';
import '../styles/dashboard.css';

export const Dashboard: React.FC = () => {
    const [metrics, setMetrics] = useState<DashboardMetrics | null>(null);
    const [connectionStatus, setConnectionStatus] = useState('connecting');
    const { isConnected, error: wsError, subscribe } = useWebSocket();
    const { metrics: polledMetrics} = useMetrics();

    useEffect(() => {
        if (isConnected) {
            setConnectionStatus('connected');
            subscribe('message', (data: DashboardMetrics) => {
                setMetrics(data);
            });
        } else {
            setConnectionStatus('disconnected');
            if (polledMetrics) {
                setMetrics(polledMetrics);
            }
        }
    }, [isConnected, subscribe, polledMetrics]);

    useEffect(() => {
        if (!isConnected && polledMetrics) {
            setMetrics(polledMetrics);
        }
    }, [polledMetrics, isConnected]);

    const getStatusColor = () => {
        if (connectionStatus === 'connected') return '#4CAF50';
        if (connectionStatus === 'disconnected') return '#FF9800';
        return '#2196F3';
    };

    const getStatusText = () => {
        if (connectionStatus === 'connected') return 'Live';
        if (connectionStatus === 'disconnected') return 'Polling';
        return 'Connecting...';
    };

    return (
        <div className="dashboard">
            <header className="dashboard-header">
                <h1>🚗 StreamRide Analytics Dashboard</h1>
                <div className="status-indicator">
          <span
              className="status-dot"
              style={{ backgroundColor: getStatusColor() }}
          ></span>
                    <span className="status-text">{getStatusText()}</span>
                </div>
            </header>

            {wsError && (
                <div className="error-banner">
                    WebSocket connection failed. Switching to polling mode.
                </div>
            )}

            <main className="dashboard-main">
                {metrics ? (
                    <>
                        <section className="metrics-section">
                            <MetricsCard
                                title="Active Rides"
                                value={metrics.activeRides}
                                icon="🚙"
                            />
                            <MetricsCard
                                title="Average Duration"
                                value={metrics.averageDuration.toFixed(2)}
                                subtitle="minutes"
                                icon="⏱️"
                            />
                            <MetricsCard
                                title="Last Updated"
                                value={new Date(metrics.timestamp).toLocaleTimeString()}
                                icon="🕐"
                            />
                        </section>

                        {metrics.topCities && metrics.topCities.length > 0 && (
                            <section className="chart-section">
                                <TopCitiesChart data={metrics.topCities} />
                            </section>
                        )}

                        {metrics.topCities && metrics.topCities.length > 0 && (
                            <section className="cities-list-section">
                                <h2>City Rankings</h2>
                                <div className="cities-list">
                                    {metrics.topCities.map((city, index) => (
                                        <div key={city.city} className="city-item">
                                            <span className="city-rank">#{index + 1}</span>
                                            <span className="city-name">{city.city}</span>
                                            <span className="city-rides">{city.activeRides} rides</span>
                                        </div>
                                    ))}
                                </div>
                            </section>
                        )}
                    </>
                ) : (
                    <div className="loading">Loading metrics...</div>
                )}
            </main>

            <footer className="dashboard-footer">
                <p>Real-time analytics powered by Apache Kafka & Spring Boot</p>
            </footer>
        </div>
    );
};