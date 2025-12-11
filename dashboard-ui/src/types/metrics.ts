export interface CityMetrics {
    city: string;
    activeRides: number;
}

export interface DashboardMetrics {
    activeRides: number;
    averageDuration: number;
    topCities: CityMetrics[];
    timestamp: string;
}

export interface WebSocketMessage {
    type: string;
    data: DashboardMetrics;
}