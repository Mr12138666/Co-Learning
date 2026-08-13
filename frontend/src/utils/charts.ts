/**
 * Chart utility functions.
 */

export interface ChartData {
  labels: string[]
  datasets: {
    label: string
    data: number[]
    backgroundColor?: string | string[]
    borderColor?: string | string[]
    borderWidth?: number
  }[]
}

export interface ChartOptions {
  responsive?: boolean
  maintainAspectRatio?: boolean
  plugins?: {
    legend?: {
      display?: boolean
      position?: 'top' | 'bottom' | 'left' | 'right'
    }
    title?: {
      display?: boolean
      text?: string
    }
  }
  scales?: {
    x?: {
      display?: boolean
      title?: {
        display?: boolean
        text?: string
      }
    }
    y?: {
      display?: boolean
      title?: {
        display?: boolean
        text?: string
      }
      beginAtZero?: boolean
    }
  }
}

/**
 * Generate random color.
 *
 * @returns Random hex color
 */
export function generateRandomColor(): string {
  return '#' + Math.floor(Math.random() * 16777215).toString(16).padStart(6, '0')
}

/**
 * Generate color palette.
 *
 * @param count Number of colors
 * @returns Array of colors
 */
export function generateColorPalette(count: number): string[] {
  const colors: string[] = []
  for (let i = 0; i < count; i++) {
    const hue = (i * 360) / count
    colors.push(`hsl(${hue}, 70%, 60%)`)
  }
  return colors
}

/**
 * Create line chart data.
 *
 * @param labels Labels
 * @param data Data points
 * @param label Dataset label
 * @param color Line color
 * @returns Chart data
 */
export function createLineChartData(
  labels: string[],
  data: number[],
  label: string = '数据',
  color: string = '#2080F0'
): ChartData {
  return {
    labels,
    datasets: [
      {
        label,
        data,
        borderColor: color,
        backgroundColor: color + '20',
        borderWidth: 2,
      },
    ],
  }
}

/**
 * Create bar chart data.
 *
 * @param labels Labels
 * @param data Data points
 * @param label Dataset label
 * @param colors Bar colors
 * @returns Chart data
 */
export function createBarChartData(
  labels: string[],
  data: number[],
  label: string = '数据',
  colors?: string[]
): ChartData {
  return {
    labels,
    datasets: [
      {
        label,
        data,
        backgroundColor: colors || generateColorPalette(data.length),
        borderWidth: 1,
      },
    ],
  }
}

/**
 * Create pie chart data.
 *
 * @param labels Labels
 * @param data Data points
 * @param colors Slice colors
 * @returns Chart data
 */
export function createPieChartData(
  labels: string[],
  data: number[],
  colors?: string[]
): ChartData {
  return {
    labels,
    datasets: [
      {
        label: '占比',
        data,
        backgroundColor: colors || generateColorPalette(data.length),
        borderWidth: 1,
      },
    ],
  }
}

/**
 * Create doughnut chart data.
 *
 * @param labels Labels
 * @param data Data points
 * @param colors Segment colors
 * @returns Chart data
 */
export function createDoughnutChartData(
  labels: string[],
  data: number[],
  colors?: string[]
): ChartData {
  return {
    labels,
    datasets: [
      {
        label: '占比',
        data,
        backgroundColor: colors || generateColorPalette(data.length),
        borderWidth: 2,
      },
    ],
  }
}

/**
 * Format number for display.
 *
 * @param value Number to format
 * @param decimals Decimal places
 * @returns Formatted number
 */
export function formatChartNumber(value: number, decimals: number = 0): string {
  if (value >= 1000000) {
    return (value / 1000000).toFixed(decimals) + 'M'
  }
  if (value >= 1000) {
    return (value / 1000).toFixed(decimals) + 'K'
  }
  return value.toFixed(decimals)
}

/**
 * Calculate percentage.
 *
 * @param value Current value
 * @param total Total value
 * @param decimals Decimal places
 * @returns Percentage string
 */
export function calculatePercentage(
  value: number,
  total: number,
  decimals: number = 1
): string {
  if (total === 0) return '0%'
  return ((value / total) * 100).toFixed(decimals) + '%'
}